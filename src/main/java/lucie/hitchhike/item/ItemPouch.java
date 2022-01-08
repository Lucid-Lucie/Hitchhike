package lucie.hitchhike.item;

import lucie.hitchhike.util.UtilText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemPouch extends Item
{
    public static final int STORAGE = 128;

    public ItemPouch()
    {
        super(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
        this.setRegistryName("pouch");
    }

    /* Text and information */

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag)
    {
        tooltip.add(UtilText.colorText(new String[]{I18n.get("tooltip.hitchhike.food") + ": ", String.valueOf(getFood(stack)), "/", String.valueOf(STORAGE)}, new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.WHITE}));
    }

    @Override
    @Nonnull
    public String getDescriptionId()
    {
        return I18n.get("item.hitchhike.pouch.empty");
    }

    /* Texture */

    public static float getTexture(ItemStack stack)
    {
        return getFood(stack) == 0 ? 0.0F : 0.1F;
    }

    /* Compound data */

    public static int getFood(ItemStack stack)
    {
        // Get food count.
        return stack.getOrCreateTag().getInt("food");
    }

    public static  void setFood(ItemStack stack, int amount)
    {
        // Add compound.
        if (stack.getTag() == null)
        {
            stack.setTag(new CompoundTag());
        }

        // Add food integer.
        if (!stack.getTag().contains("food"))
        {
            stack.getTag().putInt("food", 0);
        }

        // Add amount to stack.
        int food = stack.getTag().getInt("food");
        stack.getTag().putInt("food", food + amount);
    }
}
