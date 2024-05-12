package com.replaymod.render.capturer;

import java.io.Closeable;

public interface WorldRenderer extends Closeable {
   void renderWorld(float var1, CaptureData var2);

   void setOmnidirectional(boolean var1);
}
