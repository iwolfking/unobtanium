//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.reworks.cards;


import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

import iskallia.vault.core.Version;
import iskallia.vault.core.card.TaskLootCardModifier;
import iskallia.vault.core.data.adapter.vault.CompoundAdapter;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
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
import iskallia.vault.task.TaskContext;
import iskallia.vault.task.source.EntityTaskSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import xyz.iwolfking.unobtainium.api.compound.CardList;
import xyz.iwolfking.unobtainium.mixin.the_vault.accessors.CrateLootGeneratorAccessor;


/**
 * This mixin reworks how Resource Cards are processed.
 * It creates new field (unobtainium_active_cards) into Runner class, that stores all active player cards. (Runner is per player)
 * The original implementation (ActiveCardTaskHelper.class) was laggy and bugged as it voided over completion.
 * To avoid it, card tasks are checked just once: when reward crate is generated.
 * As cards are saved into custom field, then they are automatically saved and loaded on server restart.
 */
@Mixin(value = Runner.class, remap = false)
public abstract class BetterCardProcessingIntoRunner extends Listener
{
    /**
     * This injection loads and enables cards on server restart.
     * Field ACTIVE_CARDS are set only if player had any resource card, so it should do anything
     * if player had them.
     * This is necessary for enabling tasks to operate with Task#onAttach method call.
     */
    @Inject(method = "initServer", at = @At("TAIL"))
    private void attachActiveCards(VirtualWorld world, Vault vault, CallbackInfo ci)
    {
        this.ifPresent(ACTIVE_CARDS, cards ->
        {
            TaskContext taskContext =
                TaskContext.of(EntityTaskSource.ofUuids(JavaRandom.ofNanoTime(), this.get(ID)),
                    world.getServer());
            taskContext.setVault(vault);

            cards.forEach(card -> card.getEntries().stream().
                filter(entry -> entry.getModifier() instanceof TaskLootCardModifier).
                map(entry -> (TaskLootCardModifier) entry.getModifier()).
                forEach(modifier -> modifier.getTask().onAttach(taskContext))
            );
        });
    }


    /**
     * This injection adds CommonEvents.CRATE_AWARD_EVENT processing.
     * On CrateAwardEvent.Phase.PRE stage it adds `additionalItems` for all completed card tasks.
     */
    @Inject(method = "initServer", at = @At("TAIL"))
    private void addAwardListener(VirtualWorld world, Vault vault, CallbackInfo ci)
    {
        CommonEvents.CRATE_AWARD_EVENT.register(vault, data ->
        {
            if (data.getPhase().equals(CrateAwardEvent.Phase.POST))
            {
                // Only PRE phase is important.
                return;
            }

            this.ifPresent(ACTIVE_CARDS,
                cards -> unobtainium$processAwards(cards, data.getCrateLootGenerator()));
        });
    }


    /**
     * This injection is triggered once player joins the vault. In that situation it checks if player
     * has any resource cards in his deck, and adds these cards to the ACTIVE_CARDS field.
     * It also attaches these tasks to the vault.
     */
    @Inject(method = "lambda$onJoin$25", at = @At("TAIL"))
    private void setActiveCards(VirtualWorld world, Vault vault, ServerPlayer player, CallbackInfo ci)
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
        {
            // Create context with a specific vault.
            TaskContext taskContext =
                TaskContext.of(EntityTaskSource.ofUuids(JavaRandom.ofNanoTime(), player.getUUID()),
                    player.getServer());
            taskContext.setVault(vault);

            deck.getCards().values().stream().
                filter(Objects::nonNull).
                forEach(card -> card.getEntries().stream().
                    filter(entry -> entry.getModifier() instanceof TaskLootCardModifier).
                    map(entry -> (TaskLootCardModifier) entry.getModifier()).
                    forEach(modifier ->
                    {
                        this.setIfAbsent(ACTIVE_CARDS, CardList::new);
                        this.get(ACTIVE_CARDS).add(card);
                        modifier.getTask().onAttach(taskContext);
                    })
                );
        });
    }


    /**
     * This injection is triggered once player exits vault (by any means). Then it detaches all
     * active cards and clears the list.
     */
    @Inject(method = "onLeave", at = @At("TAIL"))
    private void removeActiveCards(VirtualWorld world, Vault vault, CallbackInfo ci)
    {
        this.ifPresent(ACTIVE_CARDS, cards ->
        {
            cards.forEach(card -> card.getEntries().stream().
                filter(entry -> entry.getModifier() instanceof TaskLootCardModifier).
                map(entry -> (TaskLootCardModifier) entry.getModifier()).
                forEach(modifier -> modifier.getTask().onDetach())
            );

            // Remove all cards
            cards.clear();
        });
    }


    /**
     * This injection is triggered on server shutdown. It detaches all active player cards.
     */
    @Inject(method = "releaseServer", at = @At("HEAD"))
    private void detachActiveCards(CallbackInfo ci)
    {
        this.ifPresent(ACTIVE_CARDS, cards ->
            cards.forEach(card -> card.getEntries().stream().
                filter(entry -> entry.getModifier() instanceof TaskLootCardModifier).
                map(entry -> (TaskLootCardModifier) entry.getModifier()).
                forEach(modifier -> modifier.getTask().onDetach())
            ));
    }


    /**
     * This method adds completed card task reward items to the generated loot table.
     * @param cards List of cards that are active in player vault.
     * @param crateLootGenerator The loot table generator.
     */
    @Unique
    private static void unobtainium$processAwards(CardList cards, CrateLootGenerator crateLootGenerator)
    {
        CrateLootGeneratorAccessor lootAccessor = (CrateLootGeneratorAccessor) crateLootGenerator;
        List<ItemStack> additionalItems = lootAccessor.getAdditionalItems();

        cards.forEach(card -> card.getEntries().stream().
            filter(entry -> entry.getModifier() instanceof TaskLootCardModifier).
            map(entry -> (TaskLootCardModifier) entry.getModifier()).
            forEach(modifier ->
            {
                if (!modifier.getTask().isCompleted())
                {
                    // task is not completed.
                    return;
                }

                if (!(modifier.getTask() instanceof ProgressConfiguredTask configuredTask))
                {
                    // Task is not progress based.
                    return;
                }

                // Get the multiplier of progress.
                int multiplier = (int) Math.floor(configuredTask.getProgress().getProgress() + 1e-6);

                List<ItemStack> itemStacks = modifier.generateLoot(card.getTier(), JavaRandom.ofNanoTime());

                for (int i = 0; i < multiplier; i++)
                {
                    // Could do stacking?
                    additionalItems.addAll(itemStacks);
                }
            })
        );
    }


    /**
     * The field registry for the Runner class.
     */
    @Shadow @Final
    public static FieldRegistry FIELDS;

    /**
     * The custom field that stores all active player cards.
     */
    @Unique
    private static final FieldKey<CardList> ACTIVE_CARDS = FieldKey.of("unobtainium_active_cards", CardList.class).
        with(Version.v1_31, CompoundAdapter.of(CardList::new), DISK.all()).
        register(FIELDS);
}
