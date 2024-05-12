package com.replaymod.mixin;

import com.replaymod.core.events.KeyBindingEventCallback;
import com.replaymod.core.events.KeyEventCallback;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardHandler.class})
public class MixinKeyboardListener2 {
   private static final String ON_KEY_PRESSED = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V";

   @Inject(
      method = {"keyPress"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"
)},
      cancellable = true
   )
   private void beforeKeyBindingTick(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
      if (KeyEventCallback.EVENT.invoker().onKeyEvent(key, scanCode, action, modifiers)) {
         ci.cancel();
      }

   }//todo is this correct

   @Inject(
      method = {"keyPress"},
      at = {@At(
   value = "INVOKE",
   target = ON_KEY_PRESSED,
   shift = Shift.AFTER
)}
   )
   private void afterKeyBindingTick(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
      KeyBindingEventCallback.EVENT.invoker().onKeybindingEvent();
   }
}
