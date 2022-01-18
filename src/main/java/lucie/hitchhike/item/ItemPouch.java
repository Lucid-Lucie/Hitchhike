package lucie.hitchhike.item;

import lucie.hitchhike.util.UtilParticle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
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
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION));
        this.setRegistryName("pouch");
    }

    /* Right click on entity */

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand)
    {
        // Check for cooldown.
        if (player.getCooldowns().isOnCooldown(this)) return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));

        // Check if riding entity and if entity is alive.
        if (player.getVehicle() == null || !player.getVehicle().getType().equals(EntityType.HORSE) || !player.getVehicle().isAlive()) return super.use(level, player, hand);

        // Get horse.
        Horse horse = (Horse) player.getVehicle();

        // Remove passenger.
        player.stopRiding();
        
        // Create data.
        create(horse, player, hand);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    /* Right click at entity */

    @Override
    @Nonnull
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand)
    {
        // Check for cooldown.
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResult.FAIL;

        // Check for horse and if it is alive.
        if (!entity.getType().equals(EntityType.HORSE) || !entity.isAlive()) return InteractionResult.FAIL;

        // Create data
        create((Horse) entity, player, hand);

        return InteractionResult.SUCCESS;
    }

    /* Shared */

    public static void create(Horse horse, Player player, InteractionHand hand)
    {
        // Create Horse Pouch.
        ItemStack pouch = new ItemStack(ItemAlias.POUCH_WITH_HORSE);
        pouch.setTag(new CompoundTag());

        // Add custom name.
        if (horse.hasCustomName())
        {
            pouch.setHoverName(horse.getCustomName());
        }

        // Save entity data
        CompoundTag content = new CompoundTag();
        horse.save(content);

        // Add data to pouch.
        pouch.getOrCreateTag().put("Content", content);

        // Data might not get set without this.
        player.setItemInHand(hand, pouch);

        // Set cooldown.
        player.getCooldowns().addCooldown(ItemAlias.POUCH, 20);
        player.getCooldowns().addCooldown(ItemAlias.POUCH_WITH_HORSE, 20);

        // Remove leash.
        if (horse.isLeashed())
        {
            horse.dropLeash(true, true);
        }

        // Remove entity.
        horse.discard();

        // Sound and particles
        if (player.level.isClientSide)
        {
            // Sound
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);

            // Poof
            UtilParticle.spawnPoofParticles(horse);
        }
    }
}
