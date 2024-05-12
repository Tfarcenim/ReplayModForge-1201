package com.replaymod.lib.de.johni0702.minecraft.gui;

import com.mojang.blaze3d.platform.GlStateManager.LogicOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.NonNull;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Color;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class MinecraftGuiRenderer implements GuiRenderer {
   private final Minecraft mc = MCVer.getMinecraft();
   private final GuiGraphics context;
   private final PoseStack matrixStack;
   @NonNull
   private final int scaledWidth;
   private final int scaledHeight;
   private final double scaleFactor;

   public MinecraftGuiRenderer(GuiGraphics context) {
      this.scaledWidth = MCVer.newScaledResolution(this.mc).getGuiScaledWidth();
      this.scaledHeight = MCVer.newScaledResolution(this.mc).getGuiScaledHeight();
      this.scaleFactor = MCVer.newScaledResolution(this.mc).getGuiScale();
      this.context = context;
      this.matrixStack = context.pose();
   }

   public ReadablePoint getOpenGlOffset() {
      return new Point(0, 0);
   }

   public GuiGraphics getContext() {
      return this.context;
   }

   public PoseStack getMatrixStack() {
      return this.matrixStack;
   }

   public ReadableDimension getSize() {
      return new ReadableDimension() {
         public int getWidth() {
            return MinecraftGuiRenderer.this.scaledWidth;
         }

         public int getHeight() {
            return MinecraftGuiRenderer.this.scaledHeight;
         }

         public void getSize(WritableDimension dest) {
            dest.setSize(this.getWidth(), this.getHeight());
         }
      };
   }

   public void setDrawingArea(int x, int y, int width, int height) {
      y = this.scaledHeight - y - height;
      int f = (int)this.scaleFactor;
      MCVer.setScissorBounds(x * f, y * f, width * f, height * f);
   }

   public void bindTexture(ResourceLocation location) {
      MCVer.bindTexture(location);
   }

   public void bindTexture(int glId) {
      RenderSystem.setShaderTexture(0, glId);
   }

   public void drawTexturedRect(int x, int y, int u, int v, int width, int height) {
      this.drawTexturedRect(x, y, u, v, width, height, width, height, 256, 256);
   }

   public void drawTexturedRect(int x, int y, int u, int v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight) {
      this.color(1.0F, 1.0F, 1.0F);
      this.drawTexturedRect(x, x + width, y, y + height, (float)u / (float)textureWidth, (float)(u + uWidth) / (float)textureWidth, (float)v / (float)textureHeight, (float)(v + vHeight) / (float)textureHeight);
   }

   private void drawTexturedRect(int x1, int x2, int y1, int y2, float u1, float u2, float v1, float v2) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      Matrix4f matrix = this.matrixStack.last().pose();
      BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
      bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      bufferBuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).uv(u1, v1).endVertex();
      bufferBuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).uv(u1, v2).endVertex();
      bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).uv(u2, v2).endVertex();
      bufferBuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).uv(u2, v1).endVertex();
      BufferUploader.drawWithShader(bufferBuilder.end());
   }

   public void drawRect(int x, int y, int width, int height, int color) {
      this.context.fill(x, y, x + width, y + height, color);
      this.color(1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
   }

   public void drawRect(int x, int y, int width, int height, ReadableColor color) {
      this.drawRect(x, y, width, height, this.color(color));
   }

   public void drawRect(int x, int y, int width, int height, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor) {
      this.drawRect(x, y, width, height, this.color(topLeftColor), this.color(topRightColor), this.color(bottomLeftColor), this.color(bottomRightColor));
   }

   public void drawRect(int x, int y, int width, int height, ReadableColor tl, ReadableColor tr, ReadableColor bl, ReadableColor br) {
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(770, 771, 1, 0);
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      MCVer.drawRect(x, y, width, height, tl, tr, bl, br);
   }

   public int drawString(int x, int y, int color, String text) {
      return this.drawString(x, y, color, text, false);
   }

   public int drawString(int x, int y, ReadableColor color, String text) {
      return this.drawString(x, y, this.color(color), text);
   }

   public int drawCenteredString(int x, int y, int color, String text) {
      return this.drawCenteredString(x, y, color, text, false);
   }

   public int drawCenteredString(int x, int y, ReadableColor color, String text) {
      return this.drawCenteredString(x, y, this.color(color), text);
   }

   public int drawString(int x, int y, int color, String text, boolean shadow) {
      Font fontRenderer = MCVer.getFontRenderer();

      int var7;
      try {
         var7 = this.context.drawString(fontRenderer, text, x, y, color, shadow);
      } finally {
         this.color(1.0F, 1.0F, 1.0F);
      }

      return var7;
   }

   public int drawString(int x, int y, ReadableColor color, String text, boolean shadow) {
      return this.drawString(x, y, this.color(color), text, shadow);
   }

   public int drawCenteredString(int x, int y, int color, String text, boolean shadow) {
      Font fontRenderer = MCVer.getFontRenderer();
      x -= fontRenderer.width(text) / 2;
      return this.drawString(x, y, color, text, shadow);
   }

   public int drawCenteredString(int x, int y, ReadableColor color, String text, boolean shadow) {
      return this.drawCenteredString(x, y, this.color(color), text, shadow);
   }

   private int color(ReadableColor color) {
      return color.getAlpha() << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
   }

   private ReadableColor color(int color) {
      return new Color(color >> 16 & 255, color >> 8 & 255, color & 255, color >> 24 & 255);
   }

   private void color(float r, float g, float b) {
      RenderSystem.setShaderColor(r, g, b, 1.0F);
   }

   public void invertColors(int right, int bottom, int left, int top) {
      if (left < right && top < bottom) {
         this.color(0.0F, 0.0F, 1.0F);
         RenderSystem.enableColorLogicOp();
         RenderSystem.logicOp(LogicOp.OR_REVERSE);
         MCVer.drawRect(right, bottom, left, top);
         RenderSystem.disableColorLogicOp();
         this.color(1.0F, 1.0F, 1.0F);
      }
   }
}
