package com.replaymod.compat;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public interface GuiScreenAccessor {
   <T extends GuiEventListener & Renderable & NarratableEntry> T invokeAddButton(T var1);
}
