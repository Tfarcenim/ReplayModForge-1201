package com.replaymod.mixin.world_border;

import com.replaymod.core.versions.MCVer;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({LevelRenderer.class})
public class Mixin_UseReplayTime_ForTexture {
   @Redirect(
      method = {"*"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/Util;getMillis()J"
)
   )
   private long getWorldBorderTime() {
      ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
      return replayHandler != null ? (long)replayHandler.getReplaySender().currentTimeStamp() : MCVer.milliTime();
   }
}
