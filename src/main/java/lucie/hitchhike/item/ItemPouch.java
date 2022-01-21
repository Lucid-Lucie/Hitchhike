package lucie.hitchhike.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class ItemPouch extends Item
{
    public ItemPouch()
    {
        super(new Item.Properties().durability(256).tab(CreativeModeTab.TAB_TRANSPORTATION));
        this.setRegistryName("pouch");
    }
}
