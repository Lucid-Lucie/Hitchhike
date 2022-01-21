package lucie.hitchhike;

import lucie.hitchhike.item.ItemAlias;
import lucie.hitchhike.item.ItemContent;
import lucie.hitchhike.item.ItemPouch;
import lucie.hitchhike.item.content.ItemHorse;
import lucie.hitchhike.item.content.ItemSkeletonHorse;
import lucie.hitchhike.item.content.ItemZombieHorse;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod(Hitchhike.MODID)
public class Hitchhike
{
    // The mod id.
    public static final String MODID = "hitchhike";

    // The mod logger.
    public static final Logger LOGGER = LogManager.getFormatterLogger("Hitchhike");

    // List of all pouches with mobs.
    public static final List<ItemContent> HORSES = Arrays.asList(ItemAlias.POUCH_WITH_HORSE, ItemAlias.POUCH_WITH_SKELETON_HORSE, ItemAlias.POUCH_WITH_ZOMBIE_HORSE);

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
            // Pouch.
            event.getRegistry().register(new ItemPouch());

            // Pouch Content.
            event.getRegistry().register(new ItemHorse());
            event.getRegistry().register(new ItemSkeletonHorse());
            event.getRegistry().register(new ItemZombieHorse());
        }
    }
}
