package me.Thelnfamous1.crafting_table_boss.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.Thelnfamous1.crafting_table_boss.CraftingTableBoss;
import me.Thelnfamous1.crafting_table_boss.entity.CraftingTableGolem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class CraftingTableGolemRenderer extends GeoEntityRenderer<CraftingTableGolem> {
	public CraftingTableGolemRenderer(EntityRendererProvider.Context renderManager) {
		super(renderManager, new CraftingTableGolemModel());
		this.heightScale = CraftingTableBoss.CTG_SCALE;
		this.widthScale = CraftingTableBoss.CTG_SCALE;
	}

	@Override
	public RenderType getRenderType(CraftingTableGolem animatable, float partialTick, PoseStack poseStack,
									MultiBufferSource bufferSource, VertexConsumer buffer, int packedLight,
									ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}

	@Override
	public void render(CraftingTableGolem animatable, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}