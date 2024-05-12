package com.replaymod.render.gui;

import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiCheckbox;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.ReplayModRender;
import com.replaymod.render.Setting;

import java.io.File;

public class GuiRenderingDone extends GuiScreen {
   public final ReplayModRender mod;
   public final File videoFile;
   public final int videoFrames;
   public final RenderSettings settings;
   public final GuiLabel infoLine1 = new GuiLabel().setI18nText("replaymod.gui.renderdone1");
   public final GuiLabel infoLine2 = new GuiLabel().setI18nText("replaymod.gui.renderdone2");
   public final GuiButton openFolder = new GuiButton().onClick(() -> MCVer.openFile(GuiRenderingDone.this.videoFile.getParentFile())).setSize(200, 20).setI18nLabel("replaymod.gui.openfolder");
   public final GuiPanel actionsPanel;
   public final GuiPanel mainPanel;
   public final GuiButton closeButton;
   public final GuiCheckbox neverOpenCheckbox;
   public final GuiPanel closePanel;

   public GuiRenderingDone(ReplayModRender mod, File videoFile, int videoFrames, RenderSettings settings) {
      this.actionsPanel = new GuiPanel().setLayout(new VerticalLayout().setSpacing(10)).addElements(null, this.openFolder);
      this.mainPanel = new GuiPanel(this).setLayout(new VerticalLayout().setSpacing(10)).addElements(new VerticalLayout.Data(0.5D), new GuiPanel().setLayout(new VerticalLayout().setSpacing(4)).addElements(null, this.infoLine1, this.infoLine2), this.actionsPanel);
      this.closeButton = new GuiButton().onClick(new Runnable() {
         public void run() {
            if (GuiRenderingDone.this.neverOpenCheckbox.isChecked()) {
               SettingsRegistry settingsRegistry = GuiRenderingDone.this.mod.getCore().getSettingsRegistry();
               settingsRegistry.set(Setting.SKIP_POST_RENDER_GUI, true);
               settingsRegistry.save();
            }

            GuiRenderingDone.this.getMinecraft().setScreen(null);
         }
      }).setSize(100, 20).setI18nLabel("replaymod.gui.close");
      this.neverOpenCheckbox = new GuiCheckbox().setI18nLabel("replaymod.gui.notagain");
      this.closePanel = new GuiPanel(this).setLayout(new HorizontalLayout(HorizontalLayout.Alignment.RIGHT).setSpacing(5)).addElements(new HorizontalLayout.Data(0.5D), this.neverOpenCheckbox, this.closeButton);
      this.setLayout(new CustomLayout<GuiScreen>() {
         protected void layout(GuiScreen container, int width, int height) {
            this.pos(GuiRenderingDone.this.mainPanel, width / 2 - this.width(GuiRenderingDone.this.mainPanel) / 2, height / 3 - this.height(GuiRenderingDone.this.mainPanel) / 2);
            this.pos(GuiRenderingDone.this.closePanel, width - 10 - this.width(GuiRenderingDone.this.closePanel), height - 10 - this.height(GuiRenderingDone.this.closePanel));
         }
      });
      this.setTitle(new GuiLabel().setI18nText("replaymod.gui.renderdonetitle"));
      this.setBackground(AbstractGuiScreen.Background.DIRT);
      this.mod = mod;
      this.videoFile = videoFile;
      this.videoFrames = videoFrames;
      this.settings = settings;
   }

   public void display() {
      if (!(Boolean)this.mod.getCore().getSettingsRegistry().get(Setting.SKIP_POST_RENDER_GUI)) {
         super.display();
      }
   }
}
