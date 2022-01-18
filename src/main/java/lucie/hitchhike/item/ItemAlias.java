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
    public static Item POUCH_WITH_HORSE;
}
