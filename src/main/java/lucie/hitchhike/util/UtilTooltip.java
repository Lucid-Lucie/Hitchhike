package lucie.hitchhike.util;

import com.mojang.datafixers.util.Pair;
import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.item.ItemHorse;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Hitchhike.MODID)
public class UtilTooltip
{
    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event)
    {
        // Get info tag if item has it.
        CompoundTag data = event.getItemStack().getTag() == null ? null : event.getItemStack().getTag().contains("Data") ? event.getItemStack().getTag().getCompound("Data") : null;

        if (event.getItemStack().getItem() instanceof ItemHorse)
        {
            int index = event.getItemStack().getEnchantmentTags().size() + 1;

            if (data == null)
            {
                // Give warning message about no data.
                TextComponent component = new TextComponent("No Data");
                component.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
                event.getToolTip().add(index, component);
            }
            else
            {
                // Space.
                event.getToolTip().add(index, new TextComponent(""));

                // Information.
                TextComponent info = new TextComponent(I18n.get("tooltip.hitchhike.information") + ":");
                info.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
                event.getToolTip().add(index + 1, info);

                // Health points, movement speed, and jump strength.
                List<Pair<String, Integer>> pairs = new ArrayList<>();
                pairs.add(new Pair<>("health", (int) Math.ceil(((data.getFloat("health") - 20) / 20) * 100)));
                pairs.add(new Pair<>("speed", (int) Math.ceil(((data.getFloat("speed") - 0.1125) / 0.3375) * 100)));
                pairs.add(new Pair<>("jump", (int) Math.ceil(((data.getFloat("jump") - 0.7) / 2.0) * 100)));

                String modifier;
                TextComponent text;

                // Calc percent.
                for (Pair<String, Integer> p : pairs)
                {
                    // Only show values that differ.
                    if (p.getSecond() == 0) continue;

                    index++;

                    // Add '+' if positive.
                    modifier = p.getSecond() > 0 ? "+" : "";

                    // Create text.
                    text = new TextComponent(modifier + p.getSecond() + "% " + I18n.get("tooltip.hitchhike." + p.getFirst()));
                    text.setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE));

                    // Add it to tooltip.
                    event.getToolTip().add(index + 1, text);
                }
            }
        }
    }
}
