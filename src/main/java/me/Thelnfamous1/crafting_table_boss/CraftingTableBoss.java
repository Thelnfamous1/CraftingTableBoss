package me.Thelnfamous1.crafting_table_boss;

import com.mojang.logging.LogUtils;
import me.Thelnfamous1.crafting_table_boss.entity.CraftingTableGolem;
import me.Thelnfamous1.crafting_table_boss.entity.ThrownBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(CraftingTableBoss.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CraftingTableBoss {
    public static final String MODID = "crafting_table_boss";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final float CTG_SCALE = 1.625F;
    public static final RegistryObject<EntityType<CraftingTableGolem>> CRAFTING_TABLE_GOLEM = registerEntityType("crafting_table_golem",
            EntityType.Builder.of(CraftingTableGolem::new, MobCategory.MONSTER)
                    .sized(2.0F * CTG_SCALE, (52.0F / 16.0F) * CTG_SCALE)
                    .clientTrackingRange(10));

    public static final RegistryObject<EntityType<ThrownBlock>> THROWN_BLOCK = registerEntityType("thrown_block",
            EntityType.Builder.<ThrownBlock>of(ThrownBlock::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(4)
                    .updateInterval(10));

    private static <T extends Entity> RegistryObject<EntityType<T>> registerEntityType(String name, EntityType.Builder<T> typeBuilder) {
        return ENTITY_TYPES.register(name, () -> typeBuilder.build(MODID + ":" + name));
    }

    public CraftingTableBoss() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modEventBus);

        if(FMLEnvironment.dist.isClient()){
            CraftingTableBossClient.initializeClient();
        }
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(CRAFTING_TABLE_GOLEM.get(), CraftingTableGolem.createCustomAttributes().build());
    }
}
