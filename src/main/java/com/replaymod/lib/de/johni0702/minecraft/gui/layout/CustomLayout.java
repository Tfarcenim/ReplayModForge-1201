package com.replaymod.lib.de.johni0702.minecraft.gui.layout;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class CustomLayout<T extends GuiContainer<T>> implements Layout {
   private final Layout parent;
   private final Map<GuiElement, Pair<Point, Dimension>> result;

   public CustomLayout() {
      this(null);
   }

   public CustomLayout(Layout parent) {
      this.result = new LinkedHashMap<>();
      this.parent = parent;
   }

   public Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> layOut(GuiContainer container, ReadableDimension size) {
      this.result.clear();
      Iterator var4;
      if (this.parent == null) {
         Collection<GuiElement> elements = container.getChildren();
         var4 = elements.iterator();

         while(var4.hasNext()) {
            GuiElement element = (GuiElement)var4.next();
            this.result.put(element, Pair.of(new Point(0, 0), new Dimension(element.getMinSize())));
         }
      } else {
         Map<GuiElement, Pair<ReadablePoint, ReadableDimension>> elements = this.parent.layOut(container, size);
         var4 = elements.entrySet().iterator();

         while(var4.hasNext()) {
            Entry<GuiElement, Pair<ReadablePoint, ReadableDimension>> entry = (Entry)var4.next();
            Pair<ReadablePoint, ReadableDimension> pair = entry.getValue();
            this.result.put(entry.getKey(), Pair.of(new Point(pair.getLeft()), new Dimension(pair.getRight())));
         }
      }

      this.layout((T)container, size.getWidth(), size.getHeight());
      return (Map)this.result;
   }

   private Pair<Point, Dimension> entry(GuiElement element) {
      return this.result.get(element);
   }

   protected void set(GuiElement element, int x, int y, int width, int height) {
      Pair<Point, Dimension> entry = this.entry(element);
      entry.getLeft().setLocation(x, y);
      entry.getRight().setSize(width, height);
   }

   protected void pos(GuiElement element, int x, int y) {
      this.entry(element).getLeft().setLocation(x, y);
   }

   protected void size(GuiElement element, ReadableDimension size) {
      size.getSize(this.entry(element).getRight());
   }

   protected void size(GuiElement element, int width, int height) {
      this.entry(element).getRight().setSize(width, height);
   }

   protected void x(GuiElement element, int x) {
      this.entry(element).getLeft().setX(x);
   }

   protected void y(GuiElement element, int y) {
      this.entry(element).getLeft().setY(y);
   }

   protected void width(GuiElement element, int width) {
      this.entry(element).getRight().setWidth(width);
   }

   protected void height(GuiElement element, int height) {
      this.entry(element).getRight().setHeight(height);
   }

   protected int x(GuiElement element) {
      return this.entry(element).getLeft().getX();
   }

   protected int y(GuiElement element) {
      return this.entry(element).getLeft().getY();
   }

   protected int width(GuiElement element) {
      return this.entry(element).getRight().getWidth();
   }

   protected int height(GuiElement element) {
      return this.entry(element).getRight().getHeight();
   }

   protected abstract void layout(T var1, int var2, int var3);

   public ReadableDimension calcMinSize(GuiContainer<?> container) {
      return new Dimension(0, 0);
   }
}
