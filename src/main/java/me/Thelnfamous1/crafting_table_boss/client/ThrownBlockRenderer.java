package me.Thelnfamous1.crafting_table_boss.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.Thelnfamous1.crafting_table_boss.entity.ThrownBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class ThrownBlockRenderer extends EntityRenderer<ThrownBlock> {
   private final BlockRenderDispatcher dispatcher;

   public ThrownBlockRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.shadowRadius = 0.5F;
      this.dispatcher = pContext.getBlockRenderDispatcher();
   }

   @Override
   public void render(ThrownBlock pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
      BlockState blockstate = pEntity.getBlockState();
      if (blockstate.getRenderShape() == RenderShape.MODEL) {
         Level level = pEntity.getLevel();
         if (blockstate != level.getBlockState(pEntity.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            pMatrixStack.pushPose();
            BlockPos blockpos = new BlockPos(pEntity.getX(), pEntity.getBoundingBox().maxY, pEntity.getZ());
            pMatrixStack.translate(-0.5D, 0.0D, -0.5D);
            var model = this.dispatcher.getBlockModel(blockstate);
            for (var renderType : model.getRenderTypes(blockstate, RandomSource.create(blockstate.getSeed(pEntity.getStartPos())), ModelData.EMPTY))
               this.dispatcher.getModelRenderer().tesselateBlock(level, model, blockstate, blockpos, pMatrixStack, pBuffer.getBuffer(renderType), false, RandomSource.create(), blockstate.getSeed(pEntity.getStartPos()), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
            pMatrixStack.popPose();
            super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
         }
      }
   }

   @Override
   public ResourceLocation getTextureLocation(ThrownBlock pEntity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}