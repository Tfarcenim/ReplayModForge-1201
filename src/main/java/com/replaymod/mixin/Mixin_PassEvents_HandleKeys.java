package com.replaymod.mixin;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({KeyboardHandler.class})
public class Mixin_PassEvents_HandleKeys {
   /*@ModifyExpressionValue(
      method = {"onKey"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
   ordinal = 0
)},
      slice = {@Slice(
   from = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"
)
)}
   )
   private Screen doesScreenPassEvents(Screen screen) {
      if (screen instanceof ScreenExt) {
         ScreenExt ext = (ScreenExt)screen;
         if (ext.doesPassEvents()) {
            screen = null;
         }
      }

      return screen;
   }*/
}
