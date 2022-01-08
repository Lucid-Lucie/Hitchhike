package lucie.hitchhike;

import lucie.hitchhike.item.InitItems;
import lucie.hitchhike.item.ItemPouch;
import lucie.hitchhike.item.ItemWheat;
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

@Mod("hitchhike")
public class Hitchhike
{
    public static final Logger LOGGER = LogManager.getFormatterLogger("Hitchhike");

    public Hitchhike()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> ItemProperties.register(InitItems.POUCH,
                new ResourceLocation("hitchhike", "model"), (stack, level, living, id) -> ItemPouch.getTexture(stack)));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event)
        {
            event.getRegistry().register(new ItemWheat());
            event.getRegistry().register(new ItemPouch());
        }
    }
}
