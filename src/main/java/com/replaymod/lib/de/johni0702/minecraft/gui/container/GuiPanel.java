package com.replaymod.lib.de.johni0702.minecraft.gui.container;

import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.Layout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GuiPanel extends AbstractGuiContainer<GuiPanel> {
   public GuiPanel() {
   }

   public GuiPanel(GuiContainer container) {
      super(container);
   }

   GuiPanel(Layout layout, int width, int height, Map<GuiElement, LayoutData> withElements) {
      this.setLayout(layout);
      if (width != 0 || height != 0) {
         this.setSize(width, height);
      }

      for (Entry<GuiElement, LayoutData> guiElementLayoutDataEntry : withElements.entrySet()) {
         this.addElements(guiElementLayoutDataEntry.getValue(), guiElementLayoutDataEntry.getKey());
      }

   }

   public static GuiPanel.GuiPanelBuilder builder() {
      return new GuiPanel.GuiPanelBuilder();
   }

   protected GuiPanel getThis() {
      return this;
   }

   public static class GuiPanelBuilder {
      private Layout layout;
      private int width;
      private int height;
      private ArrayList<GuiElement> withElements$key;
      private ArrayList<LayoutData> withElements$value;

      GuiPanelBuilder() {
      }

      public GuiPanel.GuiPanelBuilder layout(Layout layout) {
         this.layout = layout;
         return this;
      }

      public GuiPanel.GuiPanelBuilder width(int width) {
         this.width = width;
         return this;
      }

      public GuiPanel.GuiPanelBuilder height(int height) {
         this.height = height;
         return this;
      }

      public GuiPanel.GuiPanelBuilder with(GuiElement withKey, LayoutData withValue) {
         if (this.withElements$key == null) {
            this.withElements$key = new ArrayList<>();
            this.withElements$value = new ArrayList<>();
         }

         this.withElements$key.add(withKey);
         this.withElements$value.add(withValue);
         return this;
      }

      public GuiPanel.GuiPanelBuilder withElements(Map<? extends GuiElement, ? extends LayoutData> withElements) {
         if (this.withElements$key == null) {
            this.withElements$key = new ArrayList<>();
            this.withElements$value = new ArrayList<>();
         }

         for (Entry<? extends GuiElement, ? extends LayoutData> entry : withElements.entrySet()) {
            this.withElements$key.add(entry.getKey());
            this.withElements$value.add(entry.getValue());
         }

         return this;
      }

      public GuiPanel.GuiPanelBuilder clearWithElements() {
         if (this.withElements$key != null) {
            this.withElements$key.clear();
            this.withElements$value.clear();
         }

         return this;
      }

      public GuiPanel build() {
         Map<GuiElement, LayoutData> withElements;
         switch (this.withElements$key == null ? 0 : this.withElements$key.size()) {
            case 0:
               withElements = java.util.Collections.emptyMap();
               break;
            case 1:
               withElements = java.util.Collections.singletonMap(this.withElements$key.get(0),
                       this.withElements$value.get(0));
               break;
            default:
               withElements = new java.util.LinkedHashMap<>(
                       this.withElements$key.size() < 1073741824
                               ? 1 + this.withElements$key.size() + (this.withElements$key.size() - 3) / 3
                               : Integer.MAX_VALUE);
               for (int $i = 0; $i < this.withElements$key.size(); $i++)
                  withElements.put(this.withElements$key.get($i), this.withElements$value.get($i));
               withElements = java.util.Collections.unmodifiableMap(withElements);
         }

         return new GuiPanel(layout, width, height, withElements);
      }

      public String toString() {
         return "GuiPanel.GuiPanelBuilder(layout=" + this.layout + ", width=" + this.width + ", height=" + this.height + ", withElements$key=" + this.withElements$key + ", withElements$value=" + this.withElements$value + ")";
      }
   }
}
