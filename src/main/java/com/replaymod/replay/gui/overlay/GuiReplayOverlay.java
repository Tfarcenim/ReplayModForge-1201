package com.replaymod.replay.gui.overlay;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.events.KeyBindingEventCallback;
import com.replaymod.core.events.KeyEventCallback;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiOverlay;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiSlider;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.IGuiTimeline;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.WritablePoint;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.ReplaySender;
import net.minecraft.client.Options;
import net.minecraft.client.resources.language.I18n;

public class GuiReplayOverlay extends AbstractGuiOverlay<GuiReplayOverlay> {
   private final ReplayModReplay mod;
   public final GuiPanel topPanel;
   public final GuiButton playPauseButton;
   public final GuiSlider speedSlider;
   public final GuiMarkerTimeline timeline;
   public final GuiPanel statusIndicatorPanel;
   private final GuiReplayOverlay.EventHandler eventHandler;
   private boolean hidden;

   public GuiReplayOverlay(ReplayHandler replayHandler) {
      this.mod = ReplayModReplay.instance;
      this.topPanel = new GuiPanel(this).setLayout(new HorizontalLayout(HorizontalLayout.Alignment.LEFT).setSpacing(5));
      this.playPauseButton = new GuiButton() {
         public GuiElement getTooltip(RenderInfo renderInfo) {
            GuiTooltip tooltip = (GuiTooltip)super.getTooltip(renderInfo);
            if (tooltip != null) {
               String label;
               if (this.getSpriteUV().getY() == 0) {
                  label = "replaymod.gui.ingame.menu.unpause";
               } else {
                  label = "replaymod.gui.ingame.menu.pause";
               }

               String var10001 = I18n.get(label);
               tooltip.setText(var10001 + " (" + GuiReplayOverlay.this.mod.keyPlayPause.getBoundKey() + ")");
            }

            return tooltip;
         }
      }.setSize(20, 20).setTexture(ReplayMod.TEXTURE, 256).setTooltip(new GuiTooltip());
      this.speedSlider = new GuiSlider().setSize(100, 20).setSteps(37);
      this.statusIndicatorPanel = new GuiPanel(this).setSize(100, 16).setLayout(new HorizontalLayout(HorizontalLayout.Alignment.RIGHT).setSpacing(5));
      this.eventHandler = new GuiReplayOverlay.EventHandler();
      this.timeline = new GuiMarkerTimeline(replayHandler) {
         public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
            this.setCursorPosition(replayHandler.getReplaySender().currentTimeStamp());
            super.draw(renderer, size, renderInfo);
         }
      }.setSize(Integer.MAX_VALUE, 20);
      this.topPanel.addElements(null, this.playPauseButton, this.speedSlider, this.timeline);
      this.setLayout(new CustomLayout<GuiReplayOverlay>() {
         protected void layout(GuiReplayOverlay container, int width, int height) {
            this.pos(GuiReplayOverlay.this.topPanel, 10, 10);
            this.size(GuiReplayOverlay.this.topPanel, width - 20, 20);
            this.pos(GuiReplayOverlay.this.statusIndicatorPanel, width / 2, height - 21);
            this.width(GuiReplayOverlay.this.statusIndicatorPanel, width / 2 - 5);
         }
      });
      this.playPauseButton.setSpriteUV(new ReadablePoint() {
         public int getX() {
            return 0;
         }

         public int getY() {
            return replayHandler.getReplaySender().paused() ? 0 : 20;
         }

         public void getLocation(WritablePoint dest) {
            dest.setLocation(this.getX(), this.getY());
         }
      }).onClick(new Runnable() {
         public void run() {
            ReplaySender replaySender = replayHandler.getReplaySender();
            if (replaySender.paused()) {
               replaySender.setReplaySpeed(GuiReplayOverlay.this.getSpeedSliderValue());
            } else {
               replaySender.setReplaySpeed(0.0D);
            }

         }
      });
      this.speedSlider.onValueChanged(new Runnable() {
         public void run() {
            double speed = GuiReplayOverlay.this.getSpeedSliderValue();
            GuiSlider var10000 = GuiReplayOverlay.this.speedSlider;
            String var10001 = I18n.get("replaymod.gui.speed");
            var10000.setText(var10001 + ": " + speed + "x");
            ReplaySender replaySender = replayHandler.getReplaySender();
            if (!replaySender.paused()) {
               replaySender.setReplaySpeed(speed);
            }

         }
      }).setValue(9);
      this.timeline.onClick(new IGuiTimeline.OnClick() {
         public void run(int time) {
            replayHandler.doJump(time, true);
         }
      }).setLength(replayHandler.getReplayDuration());
   }

   public double getSpeedSliderValue() {
      int value = this.speedSlider.getValue() + 1;
      return value <= 9 ? (double)value / 10.0D : 1.0D + 0.25D * (double)(value - 10);
   }

   public void setVisible(boolean visible) {
      if (this.isVisible() != visible) {
         if (visible) {
            this.eventHandler.register();
         } else {
            this.eventHandler.unregister();
         }
      }

      super.setVisible(visible);
   }

   public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
      if (!this.getMinecraft().options.hideGui && !this.hidden || !this.isAllowUserInput()) {
         super.draw(renderer, size, renderInfo);
      }
   }

   protected GuiReplayOverlay getThis() {
      return this;
   }

   private class EventHandler extends EventRegistrations {
      private EventHandler() {
         this.on(KeyBindingEventCallback.EVENT, this::onKeyBindingEvent);
         this.on(KeyEventCallback.EVENT, (key, scanCode, action, modifiers) -> {
            this.onKeyInput(key, action);
            return false;
         });
      }

      private void onKeyBindingEvent() {
         Options gameSettings = GuiReplayOverlay.this.getMinecraft().options;

         while(gameSettings.keyChat.consumeClick() || gameSettings.keyCommand.consumeClick()) {
            if (!GuiReplayOverlay.this.isMouseVisible()) {
               GuiReplayOverlay.this.setMouseVisible(true);
            }
         }

      }

      private void onKeyInput(int key, int action) {
         if (action == 1) {
            if (GuiReplayOverlay.this.isMouseVisible() && key == 290) {
               GuiReplayOverlay.this.hidden = !GuiReplayOverlay.this.hidden;
            }

         }
      }
   }
}
