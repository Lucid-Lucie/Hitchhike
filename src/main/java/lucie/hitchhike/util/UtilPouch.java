package lucie.hitchhike.util;

import lucie.hitchhike.Hitchhike;
import lucie.hitchhike.item.ItemAlias;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hitchhike.MODID)
public class UtilPouch
{
    public static void addParticles(Player player, LivingEntity horse)
    {
        if (player.level.isClientSide)
        {
            // Sound
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);

            // Poof
            for(int i = 0; i < 20; ++i)
            {
                double d0 = horse.getRandom().nextGaussian() * 0.02D;
                double d1 = horse.getRandom().nextGaussian() * 0.02D;
                double d2 = horse.getRandom().nextGaussian() * 0.02D;
                horse.level.addParticle(ParticleTypes.POOF, horse.getRandomX(1.0D), horse.getRandomY(), horse.getRandomZ(1.0D), d0, d1, d2);
            }
        }
    }

    public static void addCooldown(Player player)
    {
        player.getCooldowns().addCooldown(ItemAlias.POUCH, 20);
        player.getCooldowns().addCooldown(ItemAlias.POUCH_WITH_HORSE, 20);
        player.getCooldowns().addCooldown(ItemAlias.POUCH_WITH_SKELETON_HORSE, 20);
        player.getCooldowns().addCooldown(ItemAlias.POUCH_WITH_ZOMBIE_HORSE, 20);
    }

    public static boolean isCooldown(Player player)
    {
        return player.getCooldowns().isOnCooldown(ItemAlias.POUCH) || player.getCooldowns().isOnCooldown(ItemAlias.POUCH_WITH_HORSE) || player.getCooldowns().isOnCooldown(ItemAlias.POUCH_WITH_SKELETON_HORSE) || player.getCooldowns().isOnCooldown(ItemAlias.POUCH_WITH_ZOMBIE_HORSE);
    }
}
