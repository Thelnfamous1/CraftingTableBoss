package me.Thelnfamous1.crafting_table_boss.entity;

import net.minecraft.world.entity.ai.control.BodyRotationControl;

public class CTGBodyRotationControl extends BodyRotationControl {
    private final CraftingTableGolem golem;

    public CTGBodyRotationControl(CraftingTableGolem golem) {
        super(golem);
        this.golem = golem;
    }

    @Override
    public void clientTick() {
        if(this.golem.getActiveAttackType() == CraftingTableGolem.AttackType.NONE){
            super.clientTick();
        }
    }
}
