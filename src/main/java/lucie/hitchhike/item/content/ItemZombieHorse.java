package lucie.hitchhike.item.content;

import lucie.hitchhike.item.ItemContent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.Objects;

public class ItemZombieHorse extends ItemContent
{
    public ItemZombieHorse()
    {
        super(EntityType.ZOMBIE_HORSE);
    }

    @Override
    public CompoundTag getData(AbstractHorse horse)
    {
        CompoundTag tag = new CompoundTag();

        // Get jump.
        tag.putFloat("jump", (float) Objects.requireNonNull(horse.getAttribute(Attributes.JUMP_STRENGTH)).getValue());

        // Get speed.
        tag.putFloat("speed", (float) Objects.requireNonNull(horse.getAttribute(Attributes.MOVEMENT_SPEED)).getValue());

        // Get health.
        tag.putFloat("health", horse.getMaxHealth());

        return tag;
    }
}
