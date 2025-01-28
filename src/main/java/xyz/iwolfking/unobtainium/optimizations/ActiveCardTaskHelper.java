package xyz.iwolfking.unobtainium.optimizations;


import java.util.*;

import iskallia.vault.core.card.TaskLootCardModifier;
import iskallia.vault.core.data.compound.ItemStackList;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.CrateAwardEvent;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.CrateLootGenerator;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.item.CardDeckItem;
import iskallia.vault.task.ProgressConfiguredTask;
import iskallia.vault.task.Task;
import iskallia.vault.task.TaskContext;
import iskallia.vault.task.source.EntityTaskSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import xyz.iwolfking.unobtainium.mixin.the_vault.accessors.CrateLootGeneratorAccessor;


/**
 * This simple class is a wrapper for adding common events on vault initialization and adds
 * processing methods. It can be easily be added directly into Vault class with mixin.
 */
public class ActiveCardTaskHelper
{
    /**
     * This method adds listener that can handle card processing to vault instance.
     * @param world The virtual world where vault happens.
     * @param vault The vault object instance.
     */
    public void initServer(VirtualWorld world, Vault vault)
    {
        // This task will process resource card rewards.
        CommonEvents.CRATE_AWARD_EVENT.register(vault, data ->
        {
            if (data.getPhase().equals(CrateAwardEvent.Phase.POST))
            {
                // Only PRE phase is important.
                return;
            }

            UUID playerId = data.getPlayer().getUUID();

            if (!this.entries.containsKey(playerId))
            {
                // Player does not have active card tasks
                return;
            }

            this.processAwards(playerId, data.getCrateLootGenerator());
        });

        // This task will start all resource card tasks.
        CommonEvents.LISTENER_JOIN.register(vault, data ->
        {
            // Get all player cards and register their tasks.
            data.getListener().getPlayer().ifPresent(player -> this.registerPlayerCards(player, vault));
        });

        // This will detach all active card tasks once player leaves vault.
        CommonEvents.LISTENER_LEAVE.register(vault, data ->
        {
            // Just in case of something went wrong and a tasks were not detached.
            data.getListener().getPlayer().ifPresent(this::detachCardTasks);
        });

        // This will store current resource rewards in `additional` reward list
        // and detach all tasks once player disconnects.
        CommonEvents.ENTITY_LEAVE.register(vault, data ->
        {
            if (data.getWorld() != world)
            {
                // Not vault world.
                return;
            }

            if (data.getEntity() instanceof ServerPlayer player)
            {
                this.processPartialAwards(player.getUUID(), vault);
            }
        });

        // This will reattach all resource card tasks to the vault
        CommonEvents.ENTITY_JOIN.register(vault, data ->
        {
            if (data.getWorld() != world)
            {
                // Not vault world.
                return;
            }

            if (data.getEntity() instanceof ServerPlayer player && !this.entries.containsKey(player.getUUID()))
            {
                // If player does not have register tasks, try to register them again.
                // This will catch login into vault tasks.
                this.registerPlayerCards(player, vault);
            }
        });
    }


    /**
     * This method adds completed card task reward items to the additional item list.
     * It also detaches card task.
     * @param playerUUID Player which need to be awarded.
     * @param vault The vault that stores information.
     */
    private void processPartialAwards(UUID playerUUID, Vault vault)
    {
        List<TaskEntry> taskEntries = this.entries.getOrDefault(playerUUID, Collections.emptyList());

        if (taskEntries.isEmpty())
        {
            // Nothing to do.
            return;
        }

        Listener listener = vault.get(Vault.LISTENERS).get(playerUUID);

        if (!(listener instanceof Runner runner))
        {
            // Not a vault runner task
            return;
        }

        runner.setIfAbsent(Runner.ADDITIONAL_CRATE_ITEMS, ItemStackList::create);
        ItemStackList additionalItems = runner.get(Runner.ADDITIONAL_CRATE_ITEMS);

        taskEntries.removeIf(entry ->
        {
            if (!entry.task().isCompleted())
            {
                // Detach and remove.
                entry.task().onDetach();
                return true;
            }

            if (!(entry.task() instanceof ProgressConfiguredTask configuredTask))
            {
                // Detach and remove
                entry.task().onDetach();
                return true;
            }

            // Get the multiplier of progress.
            int multiplier = (int) Math.floor(configuredTask.getProgress().getProgress() + 1e-6);

            List<ItemStack> itemStacks = entry.modifier().generateLoot(entry.cardTier(), JavaRandom.ofNanoTime());

            for (int i = 0; i < multiplier; i++)
            {
                // Could do stacking
                additionalItems.addAll(itemStacks);
            }

            // Detach task as it is already rewarded.
            entry.task().onDetach();
            return true;
        });

        // Remove all player data
        this.entries.remove(playerUUID);
    }


    /**
     * This method adds completed card task reward items to the generated loot table.
     * It also detaches card task.
     * @param playerUUID Player which need to be awarded.
     * @param crateLootGenerator The loot table generator.
     */
    private void processAwards(UUID playerUUID, CrateLootGenerator crateLootGenerator)
    {
        CrateLootGeneratorAccessor lootAccessor = (CrateLootGeneratorAccessor) crateLootGenerator;
        List<ItemStack> additionalItems = lootAccessor.getAdditionalItems();

        List<TaskEntry> taskEntries = this.entries.getOrDefault(playerUUID, Collections.emptyList());

        if (taskEntries.isEmpty())
        {
            // Nothing to process
            return;
        }

        taskEntries.removeIf(entry ->
        {
            if (!entry.task().isCompleted())
            {
                // task is not completed.
                return false;
            }

            if (!(entry.task() instanceof ProgressConfiguredTask configuredTask))
            {
                return false;
            }

            // Get the multiplier of progress.
            int multiplier = (int) Math.floor(configuredTask.getProgress().getProgress() + 1e-6);

            List<ItemStack> itemStacks = entry.modifier().generateLoot(entry.cardTier(), JavaRandom.ofNanoTime());

            for (int i = 0; i < multiplier; i++)
            {
                // Could do stacking?
                additionalItems.addAll(itemStacks);
            }

            // Detach task as it is already rewarded.
            entry.task().onDetach();

            return true;
        });
    }


    /**
     * This method finds card deck and register all cards with the task to the vault.
     * @param player Player which deck needs to be added.
     * @param vault The vault player joined.
     */
    private void registerPlayerCards(ServerPlayer player, Vault vault)
    {
        ItemStack deckStack = player.getCapability(CuriosCapability.INVENTORY).
            map(ICuriosItemHandler::getCurios).
            filter(curios -> curios.containsKey("deck")).
            map(curios -> curios.get("deck")).
            map(slot -> slot.getStacks().getStackInSlot(0)).
            orElse(ItemStack.EMPTY);

        if (deckStack.isEmpty())
        {
            // Not a deck.
            return;
        }

        CardDeckItem.getCardDeck(deckStack).ifPresent(deck ->
            deck.getCards().values().forEach(card -> card.getEntries().stream().
                filter(entry -> entry.getModifier() instanceof TaskLootCardModifier).
                map(entry -> (TaskLootCardModifier) entry.getModifier()).
                forEach(modifier ->
                {
                    // Create context with a specific vault.
                    TaskContext taskContext =
                        TaskContext.of(EntityTaskSource.ofUuids(JavaRandom.ofNanoTime(), player.getUUID()),
                            player.getServer());
                    taskContext.setVault(vault);

                    // Create task entry
                    TaskEntry newEntry = new TaskEntry(modifier.getTask(), taskContext, modifier, card.getTier());
                    this.entries.computeIfAbsent(player.getUUID(),
                        uuid -> new ArrayList<>()).add(newEntry);

                    // Attach task
                    newEntry.task().onAttach(newEntry.context());
                })
            )
        );
    }


    /**
     * This method detach player card tasks from the vault.
     * @param player Player which card tasks need to be detached.
     */
    private void detachCardTasks(ServerPlayer player)
    {
        if (!this.entries.containsKey(player.getUUID()))
        {
            return;
        }

        this.entries.get(player.getUUID()).forEach(entry -> entry.task().onDetach());
        this.entries.get(player.getUUID()).clear();
        this.entries.remove(player.getUUID());
    }


    /**
     * This record stores card data for easier access.
     * @param task The card modifier task.
     * @param context The context of task.
     * @param modifier The card modifier.
     * @param cardTier The card tier.
     */
    private record TaskEntry(Task task, TaskContext context, TaskLootCardModifier modifier, int cardTier)
    {
    }


    /**
     * This stores currently active player card tasks.
     */
    private final Map<UUID, List<TaskEntry>> entries = new HashMap<>();
}
