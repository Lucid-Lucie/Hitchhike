package lucie.hitchhike;

import lucie.hitchhike.item.ItemAlias;
import lucie.hitchhike.item.ItemHorse;
import lucie.hitchhike.item.ItemPouch;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Hitchhike.MODID)
public class Hitchhike
{
    // The mod id.
    public static final String MODID = "hitchhike";

    // The mod logger.
    public static final Logger LOGGER = LogManager.getFormatterLogger("Hitchhike");

    public Hitchhike()
    {
        // Used for texture overrides.
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        // Add texture override for horse models.
        event.enqueueWork(() -> ItemProperties.register(ItemAlias.POUCH_WITH_HORSE, new ResourceLocation("hitchhike", "model"), (stack, level, living, id) -> ItemHorse.getModel(stack)));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event)
        {
            event.getRegistry().register(new ItemPouch());

            event.getRegistry().register(new ItemHorse());
        }
    }
}
