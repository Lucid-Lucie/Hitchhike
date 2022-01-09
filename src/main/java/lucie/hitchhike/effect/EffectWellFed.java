package lucie.hitchhike.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class EffectWellFed extends MobEffect
{
    public EffectWellFed()
    {
        super(MobEffectCategory.BENEFICIAL, 0xECCB45);
        this.setRegistryName("well_fed");
    }
}
