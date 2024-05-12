package com.replaymod.replay;

import com.replaymod.mixin.MinecraftAccessor;
import com.replaymod.mixin.TimerAccessor;
import com.replaymod.core.versions.MCVer;
import net.minecraft.client.Minecraft;

public interface ReplaySender {
   int currentTimeStamp();

   default boolean paused() {
      Minecraft mc = MCVer.getMinecraft();
      TimerAccessor timer = (TimerAccessor)((MinecraftAccessor)mc).getTimer();
      return timer.getTickLength() == Float.POSITIVE_INFINITY;
   }

   void setReplaySpeed(double var1);

   double getReplaySpeed();

   boolean isAsyncMode();

   void setAsyncMode(boolean var1);

   void setSyncModeAndWait();

   void jumpToTime(int var1);

   void sendPacketsTill(int var1);
}
