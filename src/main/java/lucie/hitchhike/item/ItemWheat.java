package lucie.hitchhike.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import javax.annotation.Nonnull;

public class ItemWheat extends Item
{
    public ItemWheat()
    {
        super(new Item.Properties().rarity(Rarity.UNCOMMON).tab(CreativeModeTab.TAB_MISC));
        this.setRegistryName("gilded_wheat");
    }

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
}
