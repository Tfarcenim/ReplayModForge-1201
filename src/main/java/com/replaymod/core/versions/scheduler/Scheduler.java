package com.replaymod.core.versions.scheduler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface Scheduler {
   void runSync(Runnable var1) throws InterruptedException, ExecutionException, TimeoutException;

   void runPostStartup(Runnable var1);

   void runLaterWithoutLock(Runnable var1);

   void runLater(Runnable var1);

   void runTasks();
}
