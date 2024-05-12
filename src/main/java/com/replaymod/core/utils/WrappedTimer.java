package com.replaymod.core.utils;

import com.replaymod.mixin.TimerAccessor;
import net.minecraft.client.Timer;

public class WrappedTimer extends Timer {
   public static final float DEFAULT_MS_PER_TICK = 50.0F;
   protected final Timer wrapped;

   public WrappedTimer(Timer wrapped) {
      super(0.0F, 0L);
      this.wrapped = wrapped;
      this.copy(wrapped, this);
   }

   public int advanceTime(long sysClock) {
      this.copy(this, this.wrapped);

      int var3;
      try {
         var3 = this.wrapped.advanceTime(sysClock);
      } finally {
         this.copy(this.wrapped, this);
      }

      return var3;
   }

   protected void copy(Timer from, Timer to) {
      TimerAccessor fromA = (TimerAccessor)from;
      TimerAccessor toA = (TimerAccessor)to;
      to.partialTick = from.partialTick;
      toA.setLastSyncSysClock(fromA.getLastSyncSysClock());
      to.tickDelta = from.tickDelta;
      toA.setTickLength(fromA.getTickLength());
   }
}
