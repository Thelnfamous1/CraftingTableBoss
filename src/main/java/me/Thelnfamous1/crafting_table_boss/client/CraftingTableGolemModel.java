package me.Thelnfamous1.crafting_table_boss.client;

import me.Thelnfamous1.crafting_table_boss.CraftingTableBoss;
import me.Thelnfamous1.crafting_table_boss.entity.CraftingTableGolem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class CraftingTableGolemModel extends AnimatedGeoModel<CraftingTableGolem> {
    private static final ResourceLocation MODEL_RESOURCE = new ResourceLocation(CraftingTableBoss.MODID, "geo/entity/crafting_table_golem.geo.json");
    private static final ResourceLocation TEXTURE_RESOURCE = new ResourceLocation(CraftingTableBoss.MODID, "textures/entity/crafting_table_golem.png");
    private static final ResourceLocation ANIMATION_RESOURCE = new ResourceLocation(CraftingTableBoss.MODID, "animations/entity/crafting_table_golem.animation.json");

    @Override
    public ResourceLocation getModelResource(CraftingTableGolem craftingTableGolem) {
        return MODEL_RESOURCE;
    }

    @Override
    public ResourceLocation getTextureResource(CraftingTableGolem craftingTableGolem) {
        return TEXTURE_RESOURCE;
    }

    @Override
    public ResourceLocation getAnimationResource(CraftingTableGolem craftingTableGolem) {
        return ANIMATION_RESOURCE;
    }

    @Override
    public void setCustomAnimations(CraftingTableGolem animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        IBone head = this.getAnimationProcessor().getBone("Head");
        EntityModelData extraData = (EntityModelData)animationEvent.getExtraDataOfType(EntityModelData.class).get(0);
        if (head != null) {
            head.setRotationX(extraData.headPitch * Mth.DEG_TO_RAD);
            head.setRotationY(extraData.netHeadYaw * Mth.DEG_TO_RAD);
        }

    }
}
