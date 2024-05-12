package com.replaymod.lib.de.johni0702.minecraft.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public interface GuiRenderer {
   ReadablePoint getOpenGlOffset();

   GuiGraphics getContext();

   PoseStack getMatrixStack();

   ReadableDimension getSize();

   void setDrawingArea(int var1, int var2, int var3, int var4);

   void bindTexture(ResourceLocation var1);

   void bindTexture(int var1);

   void drawTexturedRect(int var1, int var2, int var3, int var4, int var5, int var6);

   void drawTexturedRect(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

   void drawRect(int var1, int var2, int var3, int var4, int var5);

   void drawRect(int var1, int var2, int var3, int var4, ReadableColor var5);

   void drawRect(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8);

   void drawRect(int var1, int var2, int var3, int var4, ReadableColor var5, ReadableColor var6, ReadableColor var7, ReadableColor var8);

   int drawString(int var1, int var2, int var3, String var4);

   int drawString(int var1, int var2, ReadableColor var3, String var4);

   int drawCenteredString(int var1, int var2, int var3, String var4);

   int drawCenteredString(int var1, int var2, ReadableColor var3, String var4);

   int drawString(int var1, int var2, int var3, String var4, boolean var5);

   int drawString(int var1, int var2, ReadableColor var3, String var4, boolean var5);

   int drawCenteredString(int var1, int var2, int var3, String var4, boolean var5);

   int drawCenteredString(int var1, int var2, ReadableColor var3, String var4, boolean var5);

   void invertColors(int var1, int var2, int var3, int var4);
}
