package com.replaymod.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({MouseHandler.class})
public abstract class MixinMouseListener {
   /*@Accessor
   abstract int getActiveButton();

   @Inject(
      method = {"method_1611"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void mouseDown(boolean[] result, Screen screen, double x, double y, int button, CallbackInfo ci) {
      if (MouseCallback.EVENT.invoker().mouseDown(x, y, button)) {
         result[0] = true;
         ci.cancel();
      }

   }

   @Inject(
      method = {"method_1605"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void mouseUp(boolean[] result, Screen screen, double x, double y, int button, CallbackInfo ci) {
      if (MouseCallback.EVENT.invoker().mouseUp(x, y, button)) {
         result[0] = true;
         ci.cancel();
      }

   }*/

   /*@Inject(
      method = {"method_1602"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void mouseDrag(Screen screen, double x, double y, double dx, double dy, CallbackInfo ci) {
      if (MouseCallback.EVENT.invoker().mouseDrag(x, y, this.getActiveButton(), dx, dy)) {
         ci.cancel();
      }

   }*/

 /*  @Redirect(
      method = {"onMouseScroll"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDD)Z"
)
   )
   private boolean mouseScroll(Screen element, double x, double y, double scroll) {
      return MouseCallback.EVENT.invoker().mouseScroll(x, y, scroll) || element.mouseScrolled(x, y, scroll);
   }*/
}
