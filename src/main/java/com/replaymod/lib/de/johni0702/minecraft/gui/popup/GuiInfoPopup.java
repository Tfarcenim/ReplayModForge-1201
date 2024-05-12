package com.replaymod.lib.de.johni0702.minecraft.gui.popup;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;

public class GuiInfoPopup extends AbstractGuiPopup<GuiInfoPopup> implements Typeable {
   private Runnable onClosed = () -> {
   };
   private final GuiButton closeButton = new GuiButton().setSize(150, 20).onClick(() -> {
      this.close();
      this.onClosed.run();
   }).setI18nLabel("gui.back");
   private final GuiPanel info;
   private int layer;

   public static GuiInfoPopup open(GuiContainer container, String... info) {
      GuiElement[] labels = new GuiElement[info.length];

      for(int i = 0; i < info.length; ++i) {
         labels[i] = new GuiLabel().setI18nText(info[i], new Object[0]).setColor(Colors.BLACK);
      }

      return open(container, labels);
   }

   public static GuiInfoPopup open(GuiContainer container, GuiElement... info) {
      GuiInfoPopup popup = new GuiInfoPopup(container).setBackgroundColor(Colors.DARK_TRANSPARENT);
      popup.getInfo().addElements(new VerticalLayout.Data(0.5D), info);
      popup.open();
      return popup;
   }

   public GuiInfoPopup(GuiContainer container) {
      super(container);
      this.info = new GuiPanel().setMinSize(new Dimension(320, 50)).setLayout(new VerticalLayout(VerticalLayout.Alignment.TOP).setSpacing(2));
      this.popup.setLayout(new VerticalLayout().setSpacing(10)).addElements(new VerticalLayout.Data(0.5D), this.info, this.closeButton);
   }

   public GuiInfoPopup setCloseLabel(String label) {
      this.closeButton.setLabel(label);
      return this;
   }

   public GuiInfoPopup setCloseI18nLabel(String label, Object... args) {
      this.closeButton.setI18nLabel(label, args);
      return this;
   }

   public GuiInfoPopup onClosed(Runnable onClosed) {
      this.onClosed = onClosed;
      return this;
   }

   protected GuiInfoPopup getThis() {
      return this;
   }

   public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
      if (keyCode == 256) {
         this.closeButton.onClick();
         return true;
      } else {
         return false;
      }
   }

   public GuiButton getCloseButton() {
      return this.closeButton;
   }

   public GuiPanel getInfo() {
      return this.info;
   }

   public int getLayer() {
      return this.layer;
   }

   public GuiInfoPopup setLayer(int layer) {
      this.layer = layer;
      return this;
   }
}
