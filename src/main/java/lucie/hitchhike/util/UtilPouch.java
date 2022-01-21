package lucie.hitchhike.util;

import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.item.ItemAlias;
import lucie.hitchhike.item.ItemContent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = Hitchhike.MODID)
public class UtilPouch
{
    /* Events */

    @SubscribeEvent
    public static void eventClick(PlayerInteractEvent.RightClickItem event)
    {
        List<ItemContent> horses = Arrays.asList(ItemAlias.POUCH_WITH_HORSE, ItemAlias.POUCH_WITH_SKELETON_HORSE, ItemAlias.POUCH_WITH_ZOMBIE_HORSE);

        // Release
        if (event.getItemStack().getItem() instanceof ItemContent)
        {
            InteractionResult result = release((ItemContent) event.getItemStack().getItem(), event.getPlayer(), event.getHand(), null);

            if (result.equals(InteractionResult.SUCCESS))
            {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }

        // Pouch capture riding.
        if (event.getPlayer().isPassenger() && event.getItemStack().getItem().equals(ItemAlias.POUCH))
        {
            LivingEntity entity = (LivingEntity) event.getPlayer().getVehicle();

            // Check for null.
            if (entity == null) return;

            // Pouch capture entity.
            for (ItemContent i : horses)
            {
                if (i.getHorse().equals(entity.getType()))
                {
                    InteractionResult result = capture(i, (AbstractHorse)entity, event.getPlayer(), event.getHand());

                    if (result.equals(InteractionResult.SUCCESS))
                    {
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void eventInteract(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getItemStack().getItem().equals(ItemAlias.POUCH))
        {
            List<ItemContent> horses = Arrays.asList(ItemAlias.POUCH_WITH_HORSE, ItemAlias.POUCH_WITH_SKELETON_HORSE, ItemAlias.POUCH_WITH_ZOMBIE_HORSE);

            // Pouch capture entity.
            for (ItemContent i : horses)
            {
                if (i.getHorse().equals(event.getTarget().getType()))
                {
                    InteractionResult result = capture(i, (AbstractHorse)event.getTarget(), event.getPlayer(), event.getHand());

                    if (result.equals(InteractionResult.SUCCESS))
                    {
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void eventBlock(PlayerInteractEvent.RightClickBlock event)
    {
        // Pouch release block.
        if (event.getItemStack().getItem() instanceof ItemContent)
        {
            InteractionResult result = release((ItemContent) event.getItemStack().getItem(), event.getPlayer(), event.getHand(), event.getHitVec().getLocation());

            if (result.equals(InteractionResult.SUCCESS))
            {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    /* Capture Feature */

    public static InteractionResult capture(ItemContent item, AbstractHorse horse, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        // Check for cooldown.
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return InteractionResult.FAIL;

        // Create pouch.
        ItemStack pouch = new ItemStack(item);
        if (pouch.getTag() == null) pouch.setTag(new CompoundTag());

        // Content compound.
        CompoundTag content = new CompoundTag();
        horse.save(content);

        // Data compound.
        CompoundTag data = item.getData(horse);
        System.out.println(data);

        // Add custom name.
        if (horse.hasCustomName()) pouch.setHoverName(horse.getCustomName());

        // Add data.
        pouch.getTag().put("Content", content);
        pouch.getTag().put("Data", data);

        // Add data from previous stack.
        addData(stack, pouch);

        // Set cooldown.
        addCooldown(player);

        // Particles and sound.
        addParticles(player, horse);

        // Remove entity.
        horse.discard();

        // Set item to stack.
        player.setItemInHand(hand, pouch);

        return InteractionResult.SUCCESS;
    }

    /* Release Feature */

    public static InteractionResult release(ItemContent item, Player player, InteractionHand hand, @Nullable Vec3 pos)
    {
        AbstractHorse horse = (AbstractHorse) item.getHorse().create(player.level);
        ItemStack stack = player.getItemInHand(hand);

        // Check for cooldown and horse.
        if (player.getCooldowns().isOnCooldown(stack.getItem()) || horse == null) return InteractionResult.FAIL;

        // Add data from compound.
        horse.readAdditionalSaveData(player.getItemInHand(hand).getOrCreateTag().getCompound("Content"));

        // Add pos
        if (pos != null) horse.setPos(pos);
        else horse.setPos(player.getPosition(0.0F));

        // Add rot.
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

        if (pos == null) player.startRiding(horse);

        // Set item to stack.
        player.setItemInHand(hand, pouch);

        return InteractionResult.SUCCESS;
    }

    /* Tools */

    private static void addParticles(Player player, AbstractHorse horse)
    {
        if (player.level.isClientSide)
        {
            // Sound
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
            player.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);

            // Poof
            for(int i = 0; i < 20; ++i)
            {
                double d0 = horse.getRandom().nextGaussian() * 0.02D;
                double d1 = horse.getRandom().nextGaussian() * 0.02D;
                double d2 = horse.getRandom().nextGaussian() * 0.02D;
                horse.level.addParticle(ParticleTypes.POOF, horse.getRandomX(1.0D), horse.getRandomY(), horse.getRandomZ(1.0D), d0, d1, d2);
            }
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
