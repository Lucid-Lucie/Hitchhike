package lucie.hitchhike.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.Item;

import java.util.Objects;

public class ItemContent extends Item
{
    private final EntityType<?> horse;

    public ItemContent(EntityType<?> horse)
    {
        super(new Item.Properties().durability(256));
        this.setRegistryName("pouch_with_" + Objects.requireNonNull(horse.getRegistryName()).getPath());
        this.horse = horse;
    }

    public EntityType<?> getHorse()
    {
        return horse;
    }

    public CompoundTag getData(AbstractHorse horse)
    {
        return null;
    }
}
