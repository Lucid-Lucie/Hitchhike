package lucie.hitchhike.item.content;

import lucie.hitchhike.item.ItemContent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ItemHorse extends ItemContent
{
    public ItemHorse()
    {
        super(EntityType.HORSE);
    }

    @Override
    public CompoundTag getData(AbstractHorse horse)
    {
        CompoundTag tag = new CompoundTag();

        // Get jump.
        tag.putFloat("jump", (float) Objects.requireNonNull(horse.getAttribute(Attributes.JUMP_STRENGTH)).getValue());

        // Get jump.
        tag.putFloat("speed", (float) Objects.requireNonNull(horse.getAttribute(Attributes.MOVEMENT_SPEED)).getValue());

        // Get health.
        tag.putFloat("health", (float) Objects.requireNonNull(horse.getAttribute(Attributes.MAX_HEALTH)).getValue());

        return tag;
    }

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
}
