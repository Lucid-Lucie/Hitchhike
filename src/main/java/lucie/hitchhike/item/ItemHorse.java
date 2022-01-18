package lucie.hitchhike.item;

import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.util.UtilAttributes;
import lucie.hitchhike.util.UtilAttributes.Value;
import lucie.hitchhike.util.UtilParticle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class ItemHorse extends Item
{
    public ItemHorse()
    {
        super(new Item.Properties().stacksTo(1));
        this.setRegistryName("pouch_with_horse");
    }

    /* Model Predicates */

    public static float getModel(ItemStack stack)
    {
        // Get data from compound.
        if (stack.getTag() != null && stack.getTag().contains("Content"))
        {
            // Get id from variant.
            int id = (stack.getTag().getCompound("Content").getInt("Variant")%8) + 1;

            // Convert id to model id.
            return 0.0000001F * id;
        }

        // Return error model.
        return 0.0000000F;
    }

    /* Information */

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag)
    {
        // This means the item has been spawned in without data.
        if (stack.getTag() == null || !stack.getTag().contains("Content"))
        {
            TextComponent component = new TextComponent("No Data");
            component.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
            tooltip.add(component);
            return;
        }

        // Get needed attribute data.
        UtilAttributes attributes = UtilAttributes.generate(stack.getTag().getCompound("Content").getList("Attributes", 10));

        // Health
        TextComponent health = new TextComponent(I18n.get("tooltip.hitchhike.information.health", stack.getTag().getCompound("Content").getInt("Health"), attributes.getIntHealth()));
        health.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
        tooltip.add(health);

        // Divider
        tooltip.add(TextComponent.EMPTY);

        // Information
        TextComponent information = new TextComponent(I18n.get("tooltip.hitchhike.information") + ":");
        information.setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY));
        tooltip.add(information);

        // Get data for health, jump, and speed.
        List<Value> values = Arrays.asList(attributes.getHealth(), attributes.getSpeed(), attributes.getJump());

        for (Value v : values)
        {
            if (v.getValue() != 0)
            {
                // Shows "-" if negative but this adds "+" if positive.
                String modifier = v.getValue() > 0 ? "+" : "";

                // Add all data with style.
                TextComponent text = new TextComponent(modifier + v.getValue() + "% " + I18n.get("tooltip.hitchhike." + v.getName()));
                text.setStyle(Style.EMPTY.applyFormat(ChatFormatting.BLUE));

                // Add it to tooltip
                tooltip.add(text);
            }

        }
    }

    /* Release on ground */

    @Override
    @Nonnull
    public InteractionResult useOn(@Nonnull UseOnContext context)
    {
        // Check if horse can spawn.
        if (context.getPlayer() == null || context.getPlayer().getCooldowns().isOnCooldown(this)) return InteractionResult.FAIL;

        // Check for content.
        if (!context.getItemInHand().getOrCreateTag().contains("Content"))
        {
            Hitchhike.LOGGER.warn("No data found in item, item probably spawned in without needed data. Ignoring to spawn.");
            return InteractionResult.FAIL;
        }

        // Create Horse
        create(context.getPlayer(), context.getLevel(), context.getClickLocation(), context.getItemInHand(), context.getHand());

        return InteractionResult.SUCCESS;
    }

    /* Release riding */

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand)
    {
        // Check for cooldown.
        if (player.getCooldowns().isOnCooldown(this)) return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));

        // Check for content.
        if (!player.getItemInHand(hand).getOrCreateTag().contains("Content"))
        {
            Hitchhike.LOGGER.warn("No data found in item, item probably spawned in without needed data. Ignoring to spawn.");
            return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
        }

        // Create Horse
        Horse entity = create(player, level, player.getPosition(0.0F), player.getItemInHand(hand), hand);

        // Start riding entity.
        if (entity != null) player.startRiding(entity);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    /* Shared */

    public static Horse create(Player player, Level level, Vec3 pos, ItemStack stack, InteractionHand hand)
    {
        // Set cooldown on pouch.
        player.getCooldowns().addCooldown(ItemAlias.POUCH, 20);
        player.getCooldowns().addCooldown(ItemAlias.POUCH_WITH_HORSE, 20);

        // Create entity.
        Horse horse = EntityType.HORSE.create(level);

        // Check for null
        if (horse == null)
        {
            Hitchhike.LOGGER.error("Couldn't initialize horse!");
            return null;
        }

        // Add data from compound.
        horse.readAdditionalSaveData(stack.getOrCreateTag().getCompound("Content"));

        // Add pos and rot mimicking player.
        horse.setPos(pos);
        horse.setYHeadRot(player.getYHeadRot());
        horse.setYBodyRot(player.getYHeadRot());
        horse.setYRot(player.getYRot());
        horse.setXRot(player.getXRot());

        // Check and set custom name
        if (stack.hasCustomHoverName()) horse.setCustomName(stack.getHoverName());

        // Spawn entity.
        level.addFreshEntity(horse);

        // Create new pouch.
        ItemStack pouch = new ItemStack(ItemAlias.POUCH);
        pouch.setTag(new CompoundTag());

        // Set new stack.
        player.setItemInHand(hand, pouch);

        // Sound and particles
        if (level.isClientSide)
        {
            // Sound
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);

            // Poof
            UtilParticle.spawnPoofParticles(horse);
        }

        return horse;
    }
}
