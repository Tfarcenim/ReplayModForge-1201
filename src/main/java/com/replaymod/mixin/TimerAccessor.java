package com.replaymod.mixin;

import net.minecraft.client.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Timer.class})
public interface TimerAccessor {
   @Accessor("lastMs")
   long getLastSyncSysClock();

   @Accessor("lastMs")
   void setLastSyncSysClock(long var1);

   @Accessor("msPerTick")
   float getTickLength();

   @Accessor("msPerTick")
   @Mutable
   void setTickLength(float var1);
}
