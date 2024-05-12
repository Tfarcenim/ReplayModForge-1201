package com.replaymod.lib.de.johni0702.minecraft.gui.function;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public interface Draggable extends Clickable {
   boolean mouseDrag(ReadablePoint var1, int var2, @Deprecated long var3);

   boolean mouseRelease(ReadablePoint var1, int var2);
}
