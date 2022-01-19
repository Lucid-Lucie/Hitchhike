package lucie.hitchhike.util;

import com.mojang.datafixers.util.Pair;
import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.item.ItemAlias;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = Hitchhike.MODID)
public class UtilTooltip
{
    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event)
    {
        ItemStack pouch = event.getItemStack();

        // Check for pouch.
        if (!pouch.getItem().equals(ItemAlias.POUCH_WITH_HORSE)) return;

        if (pouch.getTag() == null || !pouch.getTag().contains("Content"))
        {
            // Give warning message about no data.
            TextComponent component = new TextComponent("No Data");
            component.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
            event.getToolTip().add(component);
        }
        else
        {
            // Get attributes.
            List<Pair<String, Float>> attributes = getAttributes(pouch.getTag().getCompound("Content").getList("Attributes", 10));

            // Space.
            event.getToolTip().add(new TextComponent(""));

            // Information.
            TextComponent info = new TextComponent(I18n.get("tooltip.hitchhike.information") + ":");
            info.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
            event.getToolTip().add(info);

            // Health, Jump, and Speed
            List<Pair<String, Integer>> values = new ArrayList<>();

            // Health
            values.add(new Pair<>("health", (int) Math.ceil(((getAttribute("minecraft:generic.max_health", attributes) - 20) / 20) * 100)));

            // Jump
            values.add(new Pair<>("jump", (int) Math.ceil(((getAttribute("minecraft:horse.jump_strength", attributes) - 0.7) / 2.0) * 100)));

            // Speed
            values.add(new Pair<>("speed", (int) Math.ceil(((getAttribute("minecraft:generic.movement_speed", attributes) - 0.1125) / 0.3375) * 100)));

            for (Pair<String, Integer> v : values)
            {
                if (v.getSecond() != 0)
                {
                    // Shows "-" if negative but this adds "+" if positive.
                    String modifier = v.getSecond() > 0 ? "+" : "";

                    // Add all data with style.
                    TextComponent text = new TextComponent(modifier + v.getSecond() + "% " + I18n.get("tooltip.hitchhike." + v.getFirst()));
                    text.setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE));

                    // Add it to tooltip
                    event.getToolTip().add(text);
                }
            }
            System.out.println(pouch.getTag().getAllKeys());

        }



        // Puts debug info at the bottom.
        List<Component> reorder = new ArrayList<>();
        Iterator<Component> iterator = event.getToolTip().iterator();
        Component component;
        String text;
        int i = 0;

        while (iterator.hasNext())
        {
            component = iterator.next();
            text = component.getString();
            i++;

            if (i == 1) continue;

            if (text.contains("Durability") || text.contains("hitchhike") || text.contains("NBT"))
            {
                reorder.add(component);
                iterator.remove();
            }
        }

        // Re-add all debug info.
        event.getToolTip().addAll(reorder);
    }

    private static float getAttribute(String name, List<Pair<String, Float>> attributes)
    {
        for (Pair<String, Float> p : attributes)
        {
            if (p.getFirst().equals(name)) return p.getSecond();
        }

        Hitchhike.LOGGER.warn("Couldn't find '{}' in list.", name);
        return 0.0F;
    }

    private static List<Pair<String, Float>> getAttributes(ListTag list)
    {
        CompoundTag tag;
        Pair<String, Float> pair;
        List<Pair<String, Float>> output = new ArrayList<>();
        List<String> strings = Arrays.asList("minecraft:generic.max_health", "minecraft:generic.movement_speed", "minecraft:horse.jump_strength");

        for (int i = 0; i < list.size(); i++)
        {
            tag = list.getCompound(i);

            if (strings.contains(tag.getString("Name")))
            {
                pair = new Pair<>(tag.getString("Name"), tag.getFloat("Base"));
                output.add(pair);
            }
        }

        return output;
    }
}
