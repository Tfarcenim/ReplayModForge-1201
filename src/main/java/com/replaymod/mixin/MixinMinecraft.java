package com.replaymod.mixin;

import com.replaymod.core.events.PostRenderCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.OpenGuiScreenCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public abstract class MixinMinecraft extends ReentrantBlockableEventLoop<Runnable> implements MCVer.MinecraftMethodAccessor {
   public MixinMinecraft(String string_1) {
      super(string_1);
   }

   @Shadow
   protected abstract void handleKeybinds();

   public void replayModProcessKeyBinds() {
      this.handleKeybinds();
   }

   public void replayModExecuteTaskQueue() {
      this.runAllTasks();
   }


   @Inject(method = { "runTick" }, at = {
           @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V") })
   private void preRender(boolean unused, CallbackInfo ci) {
      PreRenderCallback.EVENT.invoker().preRender();
   }

   @Inject(method = { "runTick" }, at = {
           @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", shift = At.Shift.AFTER) })
   private void postRender(boolean unused, CallbackInfo ci) {
      PostRenderCallback.EVENT.invoker().postRender();
   }

   //////////////////////////////////////////////////

   @Inject(method = { "tick" }, at = { @At("HEAD") })
   private void preTick(CallbackInfo ci) {
      PreTickCallback.EVENT.invoker().preTick();
   }

   @Inject(method = { "setScreen" }, at = {
           @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;") })
   private void openGuiScreen(Screen newGuiScreen, CallbackInfo ci) {
      OpenGuiScreenCallback.EVENT.invoker().openGuiScreen(newGuiScreen);
   }

   /*@Inject(
      method = {"runTick"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"
)}
   )
   private void preRender(boolean unused, CallbackInfo ci) {
      PreRenderCallback.EVENT.invoker().preRender();
   }

   @Inject(
      method = {"runTick"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V",
   shift = Shift.AFTER
)}
   )
   private void postRender(boolean unused, CallbackInfo ci) {
      PostRenderCallback.EVENT.invoker().postRender();
   }*/

   /*@Inject(
           method = {"tick"},
           at = {@At("HEAD")}
   )
   private void preTick(CallbackInfo ci) {
      PreTickCallback.EVENT.invoker().preTick();
   }*/

  /* @Inject(
           method = {"setScreen"},
           at = {@At(
                   value = "FIELD",
                   target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;"
           )}
   )
   private void openGuiScreen(Screen newGuiScreen, CallbackInfo ci) {
      OpenGuiScreenCallback.EVENT.invoker().openGuiScreen(newGuiScreen);
   }*/
}
