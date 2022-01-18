package lucie.hitchhike.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class UtilAttributes
{
    private final Value health, jump, speed;

    private final int intHealth;

    public UtilAttributes(Value health, Value jump, Value speed, int intHealth)
    {
        this.health = health;
        this.jump = jump;
        this.speed = speed;
        this.intHealth = intHealth;
    }

    public static UtilAttributes generate(ListTag attributes)
    {
        CompoundTag tag;

        Value health = null, jump = null, speed = null;
        int intHealth = 0;

        for (int i = 0; i < attributes.size(); i++)
        {
            tag = attributes.getCompound(i);

            // Check for health.
            if (tag.getString("Name").equals("minecraft:generic.max_health"))
            {
                health = new Value("health", (int) Math.ceil(((tag.getFloat("Base") - 20) / 20) * 100));
                intHealth = (int) tag.getFloat("Base");
            }

            // Check for jump.
            if (tag.getString("Name").equals("minecraft:horse.jump_strength")) jump = new Value("jump", (int) Math.ceil(((tag.getFloat("Base") - 0.7) / 2.0) * 100));

            // Check for speed.
            if (tag.getString("Name").equals("minecraft:generic.movement_speed")) speed = new Value("speed", (int) Math.ceil(((tag.getFloat("Base") - 0.1125) / 0.3375) * 100));
        }

        return new UtilAttributes(health, jump, speed, intHealth);
    }

    public int getIntHealth()
    {
        return intHealth;
    }

    public Value getHealth()
    {
        return health;
    }

    public Value getJump()
    {
        return jump;
    }

    public Value getSpeed()
    {
        return speed;
    }

    public static class Value
    {
        private final String name;

        private final int value;

        public Value(String name, int value)
        {
            this.name = name;
            this.value = value;
        }

        public String getName()
        {
            return name;
        }

        public int getValue()
        {
            return value;
        }
    }
}
