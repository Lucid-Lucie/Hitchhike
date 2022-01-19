package lucie.hitchhike.item;

import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.util.UtilPouch;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
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
