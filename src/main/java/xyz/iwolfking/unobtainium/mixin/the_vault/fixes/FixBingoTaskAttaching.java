//
// Created by BONNe
// Copyright - 2025
//


package xyz.iwolfking.unobtainium.mixin.the_vault.fixes;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import iskallia.vault.task.BingoTask;
import iskallia.vault.task.ConfiguredTask;
import iskallia.vault.task.Task;
import iskallia.vault.task.TaskContext;


/**
 * After server restart bingo tasks were weird. They were marked as completed, but counter still went up.
 * The reason for it was simple: BingoTask object stores completed tasks in `settledTasks` array.
 * This array was saved together with BingoTask object and populated on loading.
 * However, when BingoObjective is initialized, it attaches all BingoTask children, regardless of their
 * completion status.
 * In situations when bingo task was completed, they were attached to vault, but BingoTask did not process them
 * in BingoTask#onTick, and never detached them from vault.
 * This mixin detaches all completed bingo tasks in BingoTask#onAttach after they were attached.
 */
@Mixin(value = BingoTask.class, remap = false)
public abstract class FixBingoTaskAttaching extends ConfiguredTask<BingoTask.Config>
{
    @Shadow private boolean[] settledTasks;


    @Shadow public abstract int getWidth();


    @Shadow public abstract int getHeight();


    @Shadow public abstract Task getChild(int index);


    @Shadow protected abstract void onComplete(Task task, TaskContext context);


    @Override
    public void onAttach(TaskContext context)
    {
        super.onAttach(context);

        // Detach all completed tasks if any of tasks are settled.
        for(int index = 0; index < this.getWidth() * this.getHeight(); ++index) {
            boolean settled = this.settledTasks[index];
            if (settled) {
                Task task = this.getChild(index);
                this.onComplete(task, context);
                task.onDetach();
            }
        }
    }
}
