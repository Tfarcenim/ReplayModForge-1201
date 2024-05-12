package com.replaymod.mixin;

import com.replaymod.replay.ReplayModReplay;
import net.minecraft.world.entity.player.ProfilePublicKey.Data;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Data.class})
public abstract class Mixin_AllowExpiredPlayerKeys {
   @Inject(
      method = {"hasExpired()Z", "hasExpired(Ljava/time/Duration;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void neverExpireWhenInReplay(CallbackInfoReturnable<Boolean> ci) {
      if (ReplayModReplay.instance.getReplayHandler() != null) {
         ci.setReturnValue(false);
      }

   }
}
