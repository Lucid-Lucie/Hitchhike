package lucie.hitchhike.item;

import lucie.hitchhike.Hitchhike;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Hitchhike.MODID)
public class ItemAlias
{
    @ObjectHolder("pouch")
    public static Item POUCH;

    @ObjectHolder("pouch_with_horse")
    public static ItemContent POUCH_WITH_HORSE;

    @ObjectHolder("pouch_with_skeleton_horse")
    public static ItemContent POUCH_WITH_SKELETON_HORSE;

    @ObjectHolder("pouch_with_zombie_horse")
    public static ItemContent POUCH_WITH_ZOMBIE_HORSE;
}
