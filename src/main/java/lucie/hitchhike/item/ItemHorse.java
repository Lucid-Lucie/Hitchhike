package lucie.hitchhike.item;

import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.util.UtilPouch;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
        super(new Item.Properties().stacksTo(1));
        this.setRegistryName("pouch_with_" + Objects.requireNonNull(horse.getRegistryName()).getPath());
        this.horse = horse;
    }

    public EntityType<?> getHorse()
    {
        return horse;
    }

    /* Conversion */

    @Override
    @Nonnull
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand)
    {
        // Check for pouch being 'pouch_with_horse' and a zombie with with a free main hand.
        if (!stack.getItem().equals(ItemAlias.POUCH_WITH_HORSE) || !entity.getType().equals(EntityType.ZOMBIE) || !entity.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || !entity.hasEffect(MobEffects.DAMAGE_BOOST)) return super.interactLivingEntity(stack, player, entity, hand);

        // Create a zombie horse.
        AbstractHorse horse = EntityType.ZOMBIE_HORSE.create(player.level);

        // Check if horse was created correctly.
        if (horse == null)
        {
            Hitchhike.LOGGER.error("Couldn't initialize Zombie Horse!");
            return InteractionResult.FAIL;
        }

        // Check for Data
        if (stack.getTag() == null || !stack.getTag().contains("Data"))
        {
            Hitchhike.LOGGER.error("Couldn't fetch data!");
            return InteractionResult.FAIL;
        }

        // Convert horse to zombie.
        Objects.requireNonNull(horse.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(stack.getTag().getCompound("Data").getFloat("health"));
        Objects.requireNonNull(horse.getAttribute(Attributes.JUMP_STRENGTH)).setBaseValue(stack.getTag().getCompound("Data").getFloat("jump"));
        Objects.requireNonNull(horse.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(stack.getTag().getCompound("Data").getFloat("speed"));
        horse.equipSaddle(null);
        horse.setTamed(true);
        horse.setHealth(stack.getTag().getCompound("Data").getFloat("health"));

        // Capture new entity.
        InteractionResultHolder<ItemStack> result = ItemPouch.capture(player, hand, horse);
        if (!result.getResult().equals(InteractionResult.SUCCESS)) return InteractionResult.FAIL;

        // Give pouch to Zombie.
        entity.setItemInHand(InteractionHand.MAIN_HAND, result.getObject());

        // Give 100% drop chance for pouch.
        ((Mob)entity).setGuaranteedDrop(EquipmentSlot.MAINHAND);

        // Remove pouch.
        player.setItemInHand(hand, ItemStack.EMPTY);

        // Sound and particles.
        if (player.level.isClientSide)
        {
            // Play infect sound.
            player.playSound(SoundEvents.ZOMBIE_INFECT, 1.0F, 1.0F);

            // Spawn smoke particles.
            for(int i = 0; i < 15; ++i)
            {
                double x = entity.getRandom().nextGaussian() * 0.02D;
                double y = entity.getRandom().nextGaussian() * 0.02D;
                double z = entity.getRandom().nextGaussian() * 0.02D;

                entity.level.addParticle(ParticleTypes.SMOKE, entity.getRandomX(1.0D), entity.getRandomY()  + 0.2, entity.getRandomZ(1.0D), x, y, z);
            }
        }

        return InteractionResult.SUCCESS;
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

        // Create new pouch.
        ItemStack pouch = new ItemStack(ItemAlias.POUCH);
        pouch.setTag(new CompoundTag());

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
