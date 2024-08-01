package me.Thelnfamous1.crafting_table_boss.mixin;

import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MeleeAttackGoal.class)
public interface MeleeAttackGoalAccessor {

    @Accessor("ticksUntilNextAttack")
    void crafting_table_boss$setTicksUntilNextAttack(int ticksUntilNextAttack);

    @Accessor("ticksUntilNextAttack")
    int crafting_table_boss$getTicksUntilNextAttack();
}
