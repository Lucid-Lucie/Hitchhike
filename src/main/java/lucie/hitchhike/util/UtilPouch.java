package lucie.hitchhike.util;

import lucie.hitchhike.item.ItemAlias;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

public class UtilPouch
{
    public static ItemStack release(AbstractHorse horse, Player player, InteractionHand hand, Vec3 pos, boolean ride)
    {
        // Add data from compound.
        horse.readAdditionalSaveData(player.getItemInHand(hand).getOrCreateTag().getCompound("Content"));

        // Add pos and rot mimicking player.
        horse.setPos(pos);
        horse.setYHeadRot(player.getYHeadRot());
        horse.setYBodyRot(player.getYHeadRot());
        horse.setYRot(player.getYRot());
        horse.setXRot(player.getXRot());

        // Check and set custom name
        if (player.getItemInHand(hand).hasCustomHoverName()) horse.setCustomName(player.getItemInHand(hand).getHoverName());

        // Spawn entity.
        player.level.addFreshEntity(horse);

        // Create new pouch.
        ItemStack pouch = new ItemStack(ItemAlias.POUCH);
        pouch.setTag(new CompoundTag());

        // Add data.
        addData(player.getItemInHand(hand), pouch);

        // Damage pouch.
        if (!player.level.isClientSide && !player.isCreative()) pouch.hurtAndBreak(1, (ServerPlayer) player, serverPlayer -> serverPlayer.broadcastBreakEvent(hand));

        // Set cooldown.
        addCooldown(player);

        // Particles and sound.
        addParticles(player, horse);

        if (ride) player.startRiding(horse);

        return pouch;
    }

    public static ItemStack capture(AbstractHorse horse, Player player, InteractionHand hand)
    {
        // Remove leash.
        if (horse.isLeashed()) horse.dropLeash(true, true);

        // Create Horse Pouch.
        ItemStack pouch = new ItemStack(ItemAlias.POUCH_WITH_HORSE);
        pouch.setTag(new CompoundTag());

        // Add custom name.
        if (horse.hasCustomName()) pouch.setHoverName(horse.getCustomName());

        // Save horse data to compound.
        CompoundTag content = new CompoundTag();
        horse.save(content);

        // Add content
        pouch.getOrCreateTag().put("Content", content);

        // Add data.
        addData(player.getItemInHand(hand), pouch);

        // Set cooldown.
        addCooldown(player);

        // Particles and sound.
        addParticles(player, horse);

        // Remove entity.
        horse.discard();

        return pouch;
    }

    private static void addParticles(Player player, AbstractHorse horse)
    {
        if (player.level.isClientSide)
        {
            // Sound
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
            player.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);

            // Poof
            UtilParticle.spawnPoofParticles(horse);
        }
    }

    private static void addCooldown(Player player)
    {
        player.getCooldowns().addCooldown(ItemAlias.POUCH, 20);
        player.getCooldowns().addCooldown(ItemAlias.POUCH_WITH_HORSE, 20);
    }

    private static void addData(ItemStack old, ItemStack pouch)
    {
        pouch.setDamageValue(old.getDamageValue());
        EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(old), pouch);
        pouch.getOrCreateTag().putInt("RepairCost", old.getOrCreateTag().getInt("RepairCost"));
    }
}
