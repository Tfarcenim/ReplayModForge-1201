package com.replaymod.mixin;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Minecraft.class})
public interface MinecraftAccessor {
   @Accessor("timer")
   Timer getTimer();

   @Accessor("timer")
   @Mutable
   void setTimer(Timer var1);

   @Accessor
   CompletableFuture<Void> getPendingReload();

   @Accessor
   void setPendingReload(CompletableFuture<Void> var1);

   @Accessor
   Queue<Runnable> getProgressTasks();

   @Accessor("delayedCrash")
   Supplier<CrashReport> getCrashReporter();

   @Accessor("pendingConnection")
   void setConnection(Connection var1);
}
