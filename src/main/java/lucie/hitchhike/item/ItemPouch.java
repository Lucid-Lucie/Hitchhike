package lucie.hitchhike.item;

import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.effect.InitEffects;
import lucie.hitchhike.util.UtilParticle;
import lucie.hitchhike.util.UtilText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class ItemPouch extends Item
{
    // Max food intake for pouch.
    public static final int STORAGE = 128;

    // Mobs that can be captured.
    public static final List<EntityType<?>> HORSES = Arrays.asList(EntityType.HORSE, EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE);

    public ItemPouch()
    {
        super(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
        this.setRegistryName("pouch");
    }

    /* Text and information */

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag)
    {
        tooltip.add(UtilText.colorText(new String[]{I18n.get("tooltip.hitchhike.food") + ": ", String.valueOf(getFood(stack)), "/", String.valueOf(STORAGE)}, new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.WHITE}));
    }

    @Override
    @Nonnull
    public String getDescriptionId(@Nonnull ItemStack stack)
    {
        return stack.getTag() == null || !stack.getTag().contains("data") ? I18n.get("item.hitchhike.pouch.empty") : I18n.get("item.hitchhike.pouch.captured");
    }

    /* Usage */

    @Override
    @Nonnull
    /* Capture Entity */
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand)
    {
        // Check for cooldown.
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return InteractionResult.FAIL;

        // Can't already have a horse captured and entity needs to be a horse.
        if (stack.getTag() == null || stack.getTag().contains("data") || !HORSES.contains(entity.getType())) return InteractionResult.FAIL;

        // Pouch has to have food.
        if (getFood(stack) < 1 && !entity.hasEffect(InitEffects.WELL_FED))
        {
            if (player.level.isClientSide)
            {
                // Display no food message & spawn particles.
                player.displayClientMessage(new TextComponent(I18n.get("status.hitchhike.no_food")), true);
                UtilParticle.spawnFailParticles(player.getRandom(), entity);
            }
            else
            {
                // Make horse mad.
                if (entity instanceof AbstractHorse) ((AbstractHorse)entity).makeMad();
            }

            return InteractionResult.SUCCESS;
        }

        // Set cooldown on pouch.
        player.getCooldowns().addCooldown(stack.getItem(), 20);

        // Write and return result.
        return writeData(entity, stack, player, hand);
    }

    @Override
    @Nonnull
    /* Release Entity */
    public InteractionResult useOn(UseOnContext context)
    {
        // Set cooldown on pouch.
        if (context.getPlayer() != null && context.getPlayer().getCooldowns().isOnCooldown(context.getItemInHand().getItem())) return InteractionResult.FAIL;

        // Check for data.
        if (!context.getItemInHand().getOrCreateTag().contains("data") || !(context.getLevel() instanceof ServerLevel)) return InteractionResult.FAIL;

        // Set cooldown on pouch.
        if (context.getPlayer() != null) context.getPlayer().getCooldowns().addCooldown(context.getItemInHand().getItem(), 20);

        return readData(context.getItemInHand(), (ServerLevel) context.getLevel(), context.getClickLocation(), context.getPlayer(), context.getHand());
    }

    /* Texture */

    public static float getTexture(ItemStack stack)
    {
        return getFood(stack) == 0 ? 0.0F : 0.1F;
    }

    /* Compound data - Food */

    public static int getFood(ItemStack stack)
    {
        // Get food count.
        return stack.getOrCreateTag().getInt("food");
    }

    public static  void setFood(ItemStack stack, int amount)
    {
        // Add compound.
        if (stack.getTag() == null)
        {
            stack.setTag(new CompoundTag());
        }

        // Add food integer.
        if (!stack.getTag().contains("food"))
        {
            stack.getTag().putInt("food", 0);
        }

        // Add amount to stack.
        int food = stack.getTag().getInt("food");
        stack.getTag().putInt("food", food + amount);
    }

    /* Compound data - Entity */

    public static InteractionResult writeData(LivingEntity entity, ItemStack stack, Player player, InteractionHand hand)
    {
        if (entity.getType().getRegistryName() == null)
        {
            Hitchhike.LOGGER.error("Couldn't find registry name for: {}", entity);
            return InteractionResult.FAIL;
        }

        // Shrink food.
        if (entity.level.isClientSide)
        {
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);

            // Well fed effect removes needing to eat.
            if (!entity.hasEffect(InitEffects.WELL_FED)) UtilParticle.spawnBreakParticles(new ItemStack(InitItems.GILDED_WHEAT), player, player.getRandom(), 10);
        }
        else
        {
            // Well fed effect removes needing to eat.
            if (!entity.hasEffect(InitEffects.WELL_FED))
            {
                setFood(stack, -1);

                // Give effect after eating.
                entity.addEffect(new MobEffectInstance(InitEffects.WELL_FED, 3600));
            }
        }

        // Save entity data onto compound.
        CompoundTag data = new CompoundTag();
        entity.save(data);

        // Merge compound with item.
        CompoundTag item = stack.getOrCreateTag();
        item.putString("entity", entity.getType().getRegistryName().toString());
        item.put("data", data);

        // Data might not get set without this.
        player.setItemInHand(hand, stack);

        // Remove physical entity.
        entity.discard();

        return InteractionResult.SUCCESS;
    }

    public static InteractionResult readData(ItemStack stack, ServerLevel level, Vec3 pos, Player player, InteractionHand hand)
    {
        // Get type from "entity" tag.
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(stack.getOrCreateTag().getString("entity")));

        // Check if registry lookup found entity.
        if (type == null)
        {
            Hitchhike.LOGGER.error("Couldn't find '{}' in registries!", stack.getOrCreateTag().getString("entity"));
            return InteractionResult.FAIL;
        }

        // Create entity.
        LivingEntity entity = (LivingEntity) type.create(level);

        // Check if entity could be spawned.
        if (entity == null)
        {
            Hitchhike.LOGGER.error("Couldn't summon entity as it is null!");
            return InteractionResult.FAIL;
        }

        // Add data from compound.
        entity.readAdditionalSaveData(stack.getOrCreateTag().getCompound("data"));

        // Add pos and rot mimicking player.
        entity.setPos(pos);
        entity.setYHeadRot(player.getYHeadRot());
        entity.setYBodyRot(player.getYHeadRot());
        entity.setYRot(player.getYRot());
        entity.setXRot(player.getXRot());


        // Spawn entity.
        level.addFreshEntity(entity);

        // Clear data.
        stack.getOrCreateTag().remove("data");
        stack.getOrCreateTag().remove("entity");

        // Set new stack.
        player.setItemInHand(hand, stack);

        return InteractionResult.SUCCESS;
    }
}
