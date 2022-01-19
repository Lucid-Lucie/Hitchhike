package lucie.hitchhike.item;

import lucie.hitchhike.util.UtilPouch;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class ItemPouch extends Item
{
    public ItemPouch()
    {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION).durability(256));
        this.setRegistryName("pouch");
    }

    /* Right click on entity */

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand)
    {
        // Check if riding entity, if entity is alive, and for cooldown.
        if (player.getVehicle() == null || !isHorse((LivingEntity) player.getVehicle()) || !player.getVehicle().isAlive() || player.getCooldowns().isOnCooldown(this)) return super.use(level, player, hand);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, UtilPouch.capture((AbstractHorse) player.getVehicle(), player, hand));
    }

    /* Right click at entity */

    @Override
    @Nonnull
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand)
    {
        // Check for horse and if it is alive.
        if (!isHorse(entity) || !entity.isAlive() || player.getCooldowns().isOnCooldown(this)) return InteractionResult.FAIL;

        // Set item.
        player.setItemInHand(hand, UtilPouch.capture((AbstractHorse) entity, player, hand));

        return InteractionResult.SUCCESS;
    }

    public static boolean isHorse(LivingEntity entity)
    {
        return entity.getType().equals(EntityType.HORSE) || entity.getType().equals(EntityType.SKELETON_HORSE) || entity.getType().equals(EntityType.ZOMBIE_HORSE);
    }
}
