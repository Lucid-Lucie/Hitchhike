package lucie.hitchhike.item;

import lucie.hitchhike.util.UtilPouch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ItemPouch extends Item
{
    public ItemPouch()
    {
        super(new Item.Properties().durability(256).tab(CreativeModeTab.TAB_TRANSPORTATION));
        this.setRegistryName("pouch");
    }

    @Override
    public boolean isValidRepairItem(@Nonnull ItemStack pouch, @Nonnull ItemStack ingredient)
    {
        return ingredient.getItem().equals(Items.LEATHER);
    }

    /* Use */

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand)
    {
        // Check if player is riding entity.
        if (player.isPassenger() && player.getVehicle() != null && player.getVehicle() instanceof LivingEntity)
        {
            // Return result.
            return capture(player, hand, (LivingEntity) player.getVehicle());
        }

        return super.use(level, player, hand);
    }

    @Override
    @Nonnull
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity entity, @Nonnull InteractionHand hand)
    {
        // Store result of capture.
        InteractionResultHolder<ItemStack> result = capture(player, hand, entity);

        // Add stack to player if successful.
        if (result.getResult().equals(InteractionResult.SUCCESS)) player.setItemInHand(hand, result.getObject());

        // Return result.
        return result.getResult();
    }

    /* Capture */

    private InteractionResultHolder<ItemStack> capture(Player player, InteractionHand hand, LivingEntity entity)
    {
        // Check for cooldown.
        if (UtilPouch.isCooldown(player))
        {
            return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
        }

        // List of horses.
        List<ItemHorse> horses = Arrays.asList(ItemAlias.POUCH_WITH_HORSE, ItemAlias.POUCH_WITH_ZOMBIE_HORSE, ItemAlias.POUCH_WITH_SKELETON_HORSE);

        // Item to create.
        ItemHorse item = null;

        // Add corresponding item for entity.
        for (ItemHorse i : horses)
        {
            if (i.getHorse().equals(entity.getType())) item = i;
        }

        // Check if item was added.
        if (item == null)
        {
            return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
        }

        // Get pouch.
        ItemStack stack = player.getItemInHand(hand);

        // Check and create compound.
        if (stack.getTag() == null) stack.setTag(new CompoundTag());

        // Remove data.
        stack.getTag().remove("display");

        // Create horse pouch.
        ItemStack pouch = new ItemStack(item);
        pouch.setTag(stack.getTag());

        // Content compound.
        CompoundTag content = new CompoundTag();
        entity.save(content);

        // Data compound.
        CompoundTag data = new CompoundTag();
        data.putFloat("jump", (float) Objects.requireNonNull(entity.getAttribute(Attributes.JUMP_STRENGTH)).getValue());
        data.putFloat("speed", (float) Objects.requireNonNull(entity.getAttribute(Attributes.MOVEMENT_SPEED)).getValue());
        data.putFloat("health", entity.getMaxHealth());

        // Add data.
        pouch.getOrCreateTag().put("Content", content);
        pouch.getOrCreateTag().put("Data", data);

        // Add custom name.
        if (entity.hasCustomName()) pouch.setHoverName(entity.getCustomName());

        // Add cooldown.
        UtilPouch.addCooldown(player);

        // Add particles and sound.
        UtilPouch.addParticles(player, entity);

        // Remove entity.
        entity.discard();

        // Return new pouch.
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, pouch);
    }
}
