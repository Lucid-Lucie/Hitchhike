package lucie.hitchhike.item;

import lucie.hitchhike.util.UtilParticle;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Random;

public class ItemWheat extends Item
{
    public ItemWheat()
    {
        super(new Item.Properties().rarity(Rarity.UNCOMMON).tab(CreativeModeTab.TAB_MISC));
        this.setRegistryName("gilded_wheat");
    }

    /* Usage Entity*/

    @Override
    @Nonnull
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand)
    {
        // Item can be used to bread all animals.
        if (entity instanceof Animal)
        {
            Animal animal = (Animal) entity;

            if (animal.canFallInLove())
            {
                if (!player.level.isClientSide)
                {
                    animal.setInLove(player);
                    stack.shrink(1);

                    // Add 6s generation as bonus.
                    if (!animal.hasEffect(MobEffects.REGENERATION))
                    {
                        animal.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0));
                    }

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.interactLivingEntity(stack, player, entity, hand);
    }

    /* Usage Pouch */

    @Override
    @Nonnull
    public UseAnim getUseAnimation(@Nonnull ItemStack stack)
    {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(@Nonnull Level level, @Nonnull LivingEntity entity, @Nonnull ItemStack stack, int tick)
    {
        if (tick%16 == 15)
        {
            if (level.isClientSide)
            {
                entity.playSound(SoundEvents.NETHER_WART_PLANTED, 1.0F, 1.0F);
                UtilParticle.spawnBreakParticles(stack, entity, new Random(), 5);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        // More items becomes longer use time.
        return Math.max(stack.getCount() / 16, 1) * 16;
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand)
    {
        // Wheat needs to be in mainhand.
        if (hand != InteractionHand.MAIN_HAND) return super.use(level, player, hand);

        // Check for pouch item.
        if (player.getItemInHand(InteractionHand.OFF_HAND).getItem().equals(InitItems.POUCH))
        {
            // Check if storage of pouch is already full.
            if (ItemPouch.getFood(player.getItemInHand(InteractionHand.OFF_HAND)) == ItemPouch.STORAGE)
            {
                // Give status message on full storage.
                if (level.isClientSide)
                {
                    player.displayClientMessage(new TextComponent(I18n.get("status.hitchhike.storage_full")), true);
                }

                return new InteractionResultHolder<>(InteractionResult.CONSUME_PARTIAL, player.getItemInHand(hand));
            }

            // Start transfer.
            player.startUsingItem(hand);
        }

        return super.use(level, player, hand);
    }

    @Override
    @Nonnull
    public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity entity)
    {
        // Get food and pouch in stack.
        ItemStack food = entity.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack pouch = entity.getItemInHand(InteractionHand.OFF_HAND);

        // Check for correct items.
        if (food.getItem().equals(InitItems.GILDED_WHEAT) && pouch.getItem().equals(InitItems.POUCH))
        {
            // Get amount of food stored in pouch.
            int count = ItemPouch.getFood(pouch);

            // Add food.
            if (count < ItemPouch.STORAGE)
            {
                int amount = Math.min(ItemPouch.STORAGE - count, food.getCount());

                if (!level.isClientSide)
                {
                    // Add food and shrink stack.
                    ItemPouch.setFood(pouch, amount);
                    food.shrink(amount);
                }
                else
                {
                    // Sound effect.
                    entity.playSound(SoundEvents.NETHER_WART_BREAK, 1.0F, 1.0F);

                    // Particles
                    UtilParticle.spawnBreakParticles(stack, entity, new Random(), 10);
                }
            }
        }

        return food;
    }
}
