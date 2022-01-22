package lucie.hitchhike.item;

import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.util.UtilPouch;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ItemHorse extends Item
{
    private final EntityType<?> horse;

    public ItemHorse(EntityType<?> horse)
    {
        super(new Item.Properties().durability(256));
        this.setRegistryName("pouch_with_" + Objects.requireNonNull(horse.getRegistryName()).getPath());
        this.horse = horse;
    }

    public EntityType<?> getHorse()
    {
        return horse;
    }

    @Override
    public boolean isValidRepairItem(@Nonnull ItemStack pouch, @Nonnull ItemStack ingredient)
    {
        return ingredient.getItem().equals(Items.LEATHER);
    }

    /* Use */

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand)
    {
        // Return result.
        return release(player, hand, null);
    }

    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context)
    {
        // Needs player.
        if (context.getPlayer() == null) return super.useOn(context);

        // Store result of release.
        InteractionResultHolder<ItemStack> result = release(context.getPlayer(), context.getHand(), context.getClickLocation());

        // Add stack to player if successful.
        if (result.getResult().equals(InteractionResult.SUCCESS)) context.getPlayer().setItemInHand(context.getHand(), result.getObject());

        // Return result.
        return result.getResult();
    }

    /* Release */

    private InteractionResultHolder<ItemStack> release(Player player, InteractionHand hand, @Nullable Vec3 pos)
    {
        // Check for cooldown.
        if (UtilPouch.isCooldown(player))
        {
            return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
        }

        // Horse Pouch.
        ItemStack stack = player.getItemInHand(hand);

        // Check for data.
        if (stack.getTag() == null || !stack.getTag().contains("Content"))
        {
            Hitchhike.LOGGER.error("Couldn't find data, item has either been spawned in incorrectly or been corrupted.");
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        // Get horse type to spawn.
        AbstractHorse horse = (AbstractHorse) this.horse.create(player.level);

        // Check if horse was created.
        if (horse == null)
        {
            Hitchhike.LOGGER.error("Horse failed to create.");
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        // Add data from stack to horse.
        horse.readAdditionalSaveData(player.getItemInHand(hand).getOrCreateTag().getCompound("Content"));

        // Add pos
        if (pos != null) horse.setPos(pos);
        else horse.setPos(player.getPosition(0.0F));

        // Add rot.
        horse.setYHeadRot(player.getYHeadRot());
        horse.setYBodyRot(player.getYHeadRot());
        horse.setYRot(player.getYRot());
        horse.setXRot(player.getXRot());

        // Check and set custom name.
        if (stack.hasCustomHoverName()) horse.setCustomName(player.getItemInHand(hand).getHoverName());

        // Remove and convert.
        stack.getTag().remove("display");
        stack.getTag().remove("Content");
        stack.getTag().remove("Info");

        // Create new pouch.
        ItemStack pouch = new ItemStack(ItemAlias.POUCH);
        pouch.setTag(stack.getTag());

        // Damage pouch.
        if (!player.level.isClientSide && !player.isCreative()) pouch.hurtAndBreak(1, (ServerPlayer) player, serverPlayer -> serverPlayer.broadcastBreakEvent(hand));

        // Add cooldown.
        UtilPouch.addCooldown(player);

        // Add particles and sound.
        UtilPouch.addParticles(player, horse);

        // Spawn entity.
        player.level.addFreshEntity(horse);

        // Ride entity if no pos is given.
        if (pos == null) player.startRiding(horse);

        // Return new pouch.
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, pouch);
    }
}
