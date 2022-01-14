package lucie.hitchhike.util;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class UtilParticle
{
    public static void spawnPoofParticles(LivingEntity entity, Random random)
    {
        for(int i = 0; i < 20; ++i)
        {
            double d0 = random.nextGaussian() * 0.02D;
            double d1 = random.nextGaussian() * 0.02D;
            double d2 = random.nextGaussian() * 0.02D;
            entity.level.addParticle(ParticleTypes.POOF, entity.getRandomX(1.0D), entity.getRandomY(), entity.getRandomZ(1.0D), d0, d1, d2);
        }
    }

    public static void spawnBreakParticles(ItemStack stack, LivingEntity entity, Random random, int count)
    {
        for(int i = 0; i < count; ++i)
        {
            Vec3 vec3 = new Vec3(((double)random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            vec3 = vec3.xRot(-entity.getXRot() * ((float)Math.PI / 180F));
            vec3 = vec3.yRot(-entity.getYRot() * ((float)Math.PI / 180F));

            double d0 = (double)(-random.nextFloat()) * 0.6D - 0.3D;

            Vec3 vec31 = new Vec3(((double)random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
            vec31 = vec31.xRot(-entity.getXRot() * ((float)Math.PI / 180F));
            vec31 = vec31.yRot(-entity.getYRot() * ((float)Math.PI / 180F));
            vec31 = vec31.add(entity.getX(), entity.getEyeY(), entity.getZ());

            if (entity.level instanceof ServerLevel)
            {
                ((ServerLevel)entity.level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), vec31.x, vec31.y, vec31.z, 1, vec3.x, vec3.y + 0.05D, vec3.z, 0.0D);
            }
            else
            {
                entity.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), vec31.x, vec31.y, vec31.z, vec3.x, vec3.y + 0.05D, vec3.z);
            }
        }
    }

    public static void spawnFailParticles(Random random, LivingEntity entity)
    {
        for(int i = 0; i < 15; ++i)
        {
            double x = random.nextGaussian() * 0.02D;
            double y = random.nextGaussian() * 0.02D;
            double z = random.nextGaussian() * 0.02D;

            if (entity.level instanceof ServerLevel)
            {
                ((ServerLevel)entity.level).sendParticles(ParticleTypes.SMOKE, entity.getRandomX(1.0D), entity.getRandomY() + 0.2, entity.getRandomZ(1.0D), 1, x, y, z, 0.0F);
            }
            else
            {
                entity.level.addParticle(ParticleTypes.SMOKE, entity.getRandomX(1.0D), entity.getRandomY()  + 0.2, entity.getRandomZ(1.0D), x, y, z);
            }
        }
    }
}
