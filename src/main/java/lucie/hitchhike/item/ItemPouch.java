package lucie.hitchhike.item;

import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.effect.InitEffects;
import lucie.hitchhike.util.UtilParticle;
import lucie.hitchhike.util.UtilText;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ItemPouch extends Item
{
    // Max food intake for pouch.
    public static final int STORAGE = 128;

    // Mobs that can be captured.
    public static final List<EntityType<?>> HORSES = Arrays.asList(EntityType.HORSE, EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE);

    public ItemPouch()
    {
        super(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION));
        this.setRegistryName("pouch");
    }

    /* Text and information */

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag)
    {
        // Display amount of food.
        tooltip.add(UtilText.colorText(new String[]{I18n.get("tooltip.hitchhike.food") + ": ", String.valueOf(getFood(stack)), "/", String.valueOf(STORAGE)}, new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.WHITE}));

        // Only display info if it actually has info.
        if (stack.getTag() == null || !stack.getTag().contains("info")) return;

        // Add information title.
        tooltip.add(new TextComponent(""));
        tooltip.add(UtilText.colorText(new String[]{I18n.get("tooltip.hitchhike.info") + ": "}, new ChatFormatting[]{stack.getRarity().color}));

        // Add speed information.
        if (stack.getTag().getCompound("info").getInt("speed") != 0)
        {
            String s = stack.getTag().getCompound("info").getInt("speed") > 0 ? "+" : "";
            tooltip.add(UtilText.colorText(new String[]{s + stack.getTag().getCompound("info").getInt("speed") + "% ", I18n.get("tooltip.hitchhike.speed")}, new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.GRAY}));
        }

        // Add jump information.
        if (stack.getTag().getCompound("info").getInt("jump") != 0)
        {
            String s = stack.getTag().getCompound("info").getInt("jump") > 0 ? "+" : "";
            tooltip.add(UtilText.colorText(new String[]{s + stack.getTag().getCompound("info").getInt("jump") + "% ", I18n.get("tooltip.hitchhike.jump")}, new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.GRAY}));
        }

        // Add health information.
        tooltip.add(UtilText.colorText(new String[]{stack.getTag().getCompound("info").getInt("health") + " ", I18n.get("tooltip.hitchhike.health")}, new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.GRAY}));
    }

    @Override
    @Nonnull
    public String getDescriptionId(@Nonnull ItemStack stack)
    {
        // Check custom name.
        if (stack.getTag() != null && stack.getTag().contains("data") && stack.getTag().getCompound("data").contains("CustomName"))
        {
            // Convert custom name.
            Component component = Component.Serializer.fromJson(stack.getTag().getCompound("data").getString("CustomName"));

            // If converted, add it.
            if (component != null) return component.getString();
        }

        // Empty pouch or captured horse.
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
                player.displayClientMessage(new TextComponent(I18n.get("status.hitchhike.pouch_empty")), true);
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
        if (!context.getItemInHand().getOrCreateTag().contains("data")) return InteractionResult.FAIL;

        // Set cooldown on pouch.
        if (context.getPlayer() != null) context.getPlayer().getCooldowns().addCooldown(context.getItemInHand().getItem(), 20);

        return readData(context.getItemInHand(), context.getLevel(), context.getClickLocation(), context.getPlayer(), context.getHand());
    }

    /* Texture */

    public static float getTexture(ItemStack stack)
    {
        if (stack.getTag() == null) return 0.0000000F;

        if (stack.getTag().contains("data"))
        {
            if (stack.getTag().getString("entity").equals(Objects.requireNonNull(EntityType.SKELETON_HORSE.getRegistryName()).toString()))
            {
                return 0.0000002F;
            }

            if (stack.getTag().getString("entity").equals(Objects.requireNonNull(EntityType.ZOMBIE_HORSE.getRegistryName()).toString()))
            {
                return 0.0000003F;
            }

            // Check and apply for horse variant.
            if (stack.getTag().getString("entity").equals(Objects.requireNonNull(EntityType.HORSE.getRegistryName()).toString()))
            {

                int id = (stack.getTag().getCompound("data").getInt("Variant")%8) + 4;

                return id >= 10 ? Float.parseFloat("0.00000" + id) : Float.parseFloat("0.000000" + id);
            }
        }

        return getFood(stack) == 0 ? 0.0F : 0.0000001F;
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
            // Sound
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);

            // Poof
            UtilParticle.spawnPoofParticles(entity, entity.getRandom());

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

        // Info compound for tooltip.
        CompoundTag info = new CompoundTag();
        info.putInt("health", (int) entity.getMaxHealth());
        info.putInt("jump", (int) Math.ceil(((entity.getAttribute(Attributes.JUMP_STRENGTH).getValue() - 0.7) / 2.0) * 100));
        info.putInt("speed", (int) Math.ceil(((entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue() - 0.1125) / 0.3375) * 100));


        // Merge compound with item.
        CompoundTag item = stack.getOrCreateTag();
        item.putString("entity", entity.getType().getRegistryName().toString());
        item.put("data", data);
        item.put("info", info);

        // Data might not get set without this.
        player.setItemInHand(hand, stack);

        // Remove physical entity.
        entity.discard();

        return InteractionResult.SUCCESS;
    }

    public static InteractionResult readData(ItemStack stack, Level level, Vec3 pos, Player player, InteractionHand hand)
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

        // Check and set custom name
        if (stack.getOrCreateTag().getCompound("data").contains("CustomName"))
        {
            Component component = Component.Serializer.fromJson(stack.getOrCreateTag().getCompound("data").getString("CustomName"));

            if (component != null)
            {
                entity.setCustomName(component);
            }
            else
            {
                Hitchhike.LOGGER.error("Couldn't serialize '{}' to component. Custom name won't be added!", stack.getOrCreateTag().getCompound("data").getString("CustomName"));
            }
        }

        // Spawn entity.
        level.addFreshEntity(entity);

        // Clear data.
        stack.getOrCreateTag().remove("data");
        stack.getOrCreateTag().remove("entity");
        stack.getOrCreateTag().remove("info");

        // Set new stack.
        player.setItemInHand(hand, stack);
        
        // Sound and particles
        if (level.isClientSide)
        {
            // Sound
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);

            // Poof
            UtilParticle.spawnPoofParticles(entity, entity.getRandom());
        }

        return InteractionResult.SUCCESS;
    }
}
