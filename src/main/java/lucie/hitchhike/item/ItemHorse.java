package lucie.hitchhike.item;

import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.util.UtilAttributes;
import lucie.hitchhike.util.UtilAttributes.Value;
import lucie.hitchhike.util.UtilPouch;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ItemHorse extends Item
{
    public ItemHorse()
    {
        super(new Item.Properties().stacksTo(1).durability(256));
        this.setRegistryName("pouch_with_horse");
    }

    /* Model Predicates */

    public static float getModel(ItemStack stack)
    {
        // Get data from compound.
        if (stack.getTag() != null && stack.getTag().contains("Content"))
        {
            // Get id from variant.
            int id = (stack.getTag().getCompound("Content").getInt("Variant")%8) + 1;

            // Convert id to model id.
            return 0.0000001F * id;
        }

        // Return error model.
        return 0.0000000F;
    }

    /* Information */

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag)
    {
        super.appendHoverText(stack, level, tooltip, flag);

        // This means the item has been spawned in without data.
        if (stack.getTag() == null || !stack.getTag().contains("Content"))
        {
            TextComponent component = new TextComponent("No Data");
            component.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
            tooltip.add(component);
            return;
        }

        // Get needed attribute data.
        UtilAttributes attributes = UtilAttributes.generate(stack.getTag().getCompound("Content").getList("Attributes", 10));

        // Health
        TextComponent health = new TextComponent(I18n.get("tooltip.hitchhike.information.health", stack.getTag().getCompound("Content").getInt("Health"), attributes.getIntHealth()));
        health.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
        tooltip.add(health);

        // Divider
        tooltip.add(TextComponent.EMPTY);

        // Information
        TextComponent information = new TextComponent(I18n.get("tooltip.hitchhike.information") + ":");
        information.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
        tooltip.add(information);

        // Get data for health, jump, and speed.
        List<Value> values = Arrays.asList(attributes.getHealth(), attributes.getSpeed(), attributes.getJump());

        for (Value v : values)
        {
            if (v.getValue() != 0)
            {
                // Shows "-" if negative but this adds "+" if positive.
                String modifier = v.getValue() > 0 ? "+" : "";

                // Add all data with style.
                TextComponent text = new TextComponent(modifier + v.getValue() + "% " + I18n.get("tooltip.hitchhike." + v.getName()));
                text.setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE));

                // Add it to tooltip
                tooltip.add(text);
            }
        }
    }

    /* Release on ground */

    @Override
    @Nonnull
    public InteractionResult useOn(@Nonnull UseOnContext context)
    {
        // Check for cooldown and data.
        if (!check(context.getPlayer(), context.getItemInHand())) return InteractionResult.FAIL;

        // Release.
        context.getPlayer().setItemInHand(context.getHand(), UtilPouch.release(Objects.requireNonNull(EntityType.HORSE.create(context.getLevel())), context.getPlayer(), context.getHand(), context.getClickLocation(), false));

        return InteractionResult.CONSUME;
    }

    /* Release riding */

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand)
    {
        // Check for cooldown and data.
        if (!check(player, player.getItemInHand(hand))) return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));

        // Release.
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, UtilPouch.release(Objects.requireNonNull(EntityType.HORSE.create(level)), player, hand, player.getPosition(0.0F), true));
    }

    /* Shared */

    private boolean check(Player player, ItemStack stack)
    {
        // Check for cooldown.
        if (player == null || player.getCooldowns().isOnCooldown(this)) return false;

        // Check for content.
        if (!stack.getOrCreateTag().contains("Content"))
        {
            Hitchhike.LOGGER.warn("No data found in item, item probably spawned in without needed data. Ignoring to spawn.");
            return false;
        }

        return true;
    }
}
