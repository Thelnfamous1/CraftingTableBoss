package me.Thelnfamous1.crafting_table_boss.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RangedAttackGoal.class)
public interface RangedAttackGoalAccessor {

    @Accessor("mob")
    Mob crafting_table_boss$getMob();
}
