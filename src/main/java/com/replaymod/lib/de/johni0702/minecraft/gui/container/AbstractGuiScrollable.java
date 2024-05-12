package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.OffsetGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Scrollable;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritablePoint;

public abstract class AbstractGuiScrollable<T extends AbstractGuiScrollable<T>> extends AbstractGuiContainer<T> implements Scrollable {
   private int offsetX;
   private int offsetY;
   private final ReadablePoint negativeOffset = new ReadablePoint() {
      public int getX() {
         return -AbstractGuiScrollable.this.offsetX;
      }

      public int getY() {
         return -AbstractGuiScrollable.this.offsetY;
      }

      public void getLocation(WritablePoint dest) {
         dest.setLocation(this.getX(), this.getY());
      }
   };
   private AbstractGuiScrollable.Direction scrollDirection;
   protected ReadableDimension lastRenderSize;

   public AbstractGuiScrollable() {
      this.scrollDirection = AbstractGuiScrollable.Direction.VERTICAL;
   }

   public AbstractGuiScrollable(GuiContainer<T> container) {
      super(container);
      this.scrollDirection = AbstractGuiScrollable.Direction.VERTICAL;
   }

   public void convertFor(GuiElement element, Point point, int relativeLayer) {
      super.convertFor(element, point, relativeLayer);
      if (relativeLayer <= 0 && (point.getX() <= 0 || point.getX() >= this.lastRenderSize.getWidth() || point.getY() <= 0 || point.getY() >= this.lastRenderSize.getHeight())) {
         point.setLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
      } else {
         point.translate(this.offsetX, this.offsetY);
      }

   }

   public void layout(ReadableDimension size, RenderInfo renderInfo) {
      if (size != null) {
         int width = size.getWidth();
         int height = size.getHeight();
         this.lastRenderSize = size;
         size = super.calcMinSize();
         size = new Dimension(Math.max(width, size.getWidth()), Math.max(height, size.getHeight()));
         renderInfo = renderInfo.offsetMouse(-this.offsetX, -this.offsetY);
      }

      super.layout(size, renderInfo);
   }

   public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
      int width = size.getWidth();
      int height = size.getHeight();
      size = super.calcMinSize();
      size = new Dimension(Math.max(width, size.getWidth()), Math.max(height, size.getHeight()));
      renderInfo = renderInfo.offsetMouse(-this.offsetX, -this.offsetY);
      OffsetGuiRenderer offsetRenderer = new OffsetGuiRenderer(renderer, this.negativeOffset, size, renderInfo.layer == 0);
      offsetRenderer.startUsing();
      super.draw(offsetRenderer, size, renderInfo);
      offsetRenderer.stopUsing();
   }

   public ReadableDimension calcMinSize() {
      return new Dimension(0, 0);
   }

   public boolean scroll(ReadablePoint mousePosition, int dWheel) {
      Point mouse = new Point(mousePosition);
      if (this.getContainer() != null) {
         this.getContainer().convertFor(this, mouse);
      }

      if (mouse.getX() > 0 && mouse.getY() > 0 && mouse.getX() < this.lastRenderSize.getWidth() && mouse.getY() < this.lastRenderSize.getHeight()) {
         dWheel = (int)Math.copySign(Math.ceil((double)Math.abs(dWheel) / 4.0D), dWheel);
         if (this.scrollDirection == AbstractGuiScrollable.Direction.HORIZONTAL) {
            this.scrollX(dWheel);
         } else {
            this.scrollY(dWheel);
         }

         return true;
      } else {
         return false;
      }
   }

   public int getOffsetX() {
      return this.offsetX;
   }

   public T setOffsetX(int offsetX) {
      this.offsetX = offsetX;
      return this.getThis();
   }

   public int getOffsetY() {
      return this.offsetY;
   }

   public T setOffsetY(int offsetY) {
      this.offsetY = offsetY;
      return this.getThis();
   }

   public AbstractGuiScrollable.Direction getScrollDirection() {
      return this.scrollDirection;
   }

   public T setScrollDirection(AbstractGuiScrollable.Direction scrollDirection) {
      this.scrollDirection = scrollDirection;
      return this.getThis();
   }

   public T scrollX(int dPixel) {
      this.offsetX = Math.max(0, Math.min(super.calcMinSize().getWidth() - this.lastRenderSize.getWidth(), this.offsetX - dPixel));
      return this.getThis();
   }

   public T scrollY(int dPixel) {
      this.offsetY = Math.max(0, Math.min(super.calcMinSize().getHeight() - this.lastRenderSize.getHeight(), this.offsetY - dPixel));
      return this.getThis();
   }

   public enum Direction {
      VERTICAL,
      HORIZONTAL;

      // $FF: synthetic method
      private static AbstractGuiScrollable.Direction[] $values() {
         return new AbstractGuiScrollable.Direction[]{VERTICAL, HORIZONTAL};
      }
   }
}
