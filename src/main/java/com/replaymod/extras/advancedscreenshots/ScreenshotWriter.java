package com.replaymod.extras.advancedscreenshots;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;
import com.replaymod.extras.ReplayModExtras;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;
import com.replaymod.render.frame.BitmapFrame;
import com.replaymod.render.rendering.Channel;
import com.replaymod.render.rendering.FrameConsumer;
import com.replaymod.replay.ReplayModReplay;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import net.minecraft.CrashReport;

public class ScreenshotWriter implements FrameConsumer<BitmapFrame> {
   private final File outputFile;

   public ScreenshotWriter(File outputFile) {
      this.outputFile = outputFile;
   }

   public void consume(Map<Channel, BitmapFrame> channels) {
      BitmapFrame frame = (BitmapFrame)channels.get(Channel.BRGA);
      if (frame.getFrameId() != 0) {
         ReadableDimension frameSize = frame.getSize();

         CrashReport report;
         try {
            Image img = new Image(frameSize.getWidth(), frameSize.getHeight());

            try {
               int y = 0;

               while(true) {
                  if (y >= frameSize.getHeight()) {
                     this.outputFile.getParentFile().mkdirs();
                     img.writePNG(this.outputFile);
                     break;
                  }

                  for(int x = 0; x < frameSize.getWidth(); ++x) {
                     byte b = frame.getByteBuffer().get();
                     byte g = frame.getByteBuffer().get();
                     byte r = frame.getByteBuffer().get();
                     byte a = frame.getByteBuffer().get();
                     img.setRGBA(x, y, r, g, b, 255);
                  }

                  ++y;
               }
            } catch (Throwable var12) {
               try {
                  img.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }

               throw var12;
            }

            img.close();
         } catch (OutOfMemoryError var13) {
            var13.printStackTrace();
            report = CrashReport.forThrowable(var13, "Exporting frame");
            MCVer.getMinecraft().delayCrashRaw(report);
         } catch (Throwable var14) {
            report = CrashReport.forThrowable(var14, "Exporting frame");
            ReplayMod.instance.runLater(() -> {
               Utils.error(ReplayModExtras.LOGGER, ReplayModReplay.instance.getReplayHandler().getOverlay(), report, (Runnable)null);
            });
         }

      }
   }

   public void close() throws IOException {
   }

   public boolean isParallelCapable() {
      return false;
   }
}
