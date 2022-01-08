package lucie.hitchhike.util;

import lucie.hitchhike.Hitchhike;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class UtilText
{
    public static TextComponent colorText(String[] strings, ChatFormatting[] formattings)
    {
        TextComponent output = new TextComponent("");
        TextComponent builder;
        Style style;

        // Each string corresponds to color so they need to match.
        if (strings.length != formattings.length)
        {
            Hitchhike.LOGGER.error("colorText mismatch! Strings and colors need to match! Strings: '" + strings.length + "', Colors: '" + formattings.length + "'");
            return output;
        }

        // Add color to all strings.
        for (int i = 0; i < strings.length; i++)
        {
            // Get color.
            style = Style.EMPTY.applyFormat(formattings[i]);

            // Get string.
            builder = new TextComponent(strings[i]);

            // Combine.
            builder.setStyle(style);
            output.append(builder);
        }

        return output;
    }
}
