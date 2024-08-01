package me.Thelnfamous1.crafting_table_boss;

import me.Thelnfamous1.crafting_table_boss.client.CraftingTableGolemRenderer;
import me.Thelnfamous1.crafting_table_boss.client.ThrownBlockRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CraftingTableBossClient {

    public static void initializeClient(){

    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    public static void onClientSetup(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CraftingTableBoss.CRAFTING_TABLE_GOLEM.get(), CraftingTableGolemRenderer::new);
        event.registerEntityRenderer(CraftingTableBoss.THROWN_BLOCK.get(), ThrownBlockRenderer::new);
    }
}
