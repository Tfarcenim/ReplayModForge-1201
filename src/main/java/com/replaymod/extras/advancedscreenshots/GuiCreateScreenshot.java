package com.replaymod.extras.advancedscreenshots;

import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Loadable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.GridLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.LayoutData;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.render.RenderSettings;
import com.replaymod.render.ReplayModRender;
import com.replaymod.render.gui.GuiRenderSettings;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replaystudio.pathing.path.Timeline;
import java.io.File;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import net.minecraft.CrashReport;
import net.minecraft.client.gui.screens.Screen;

public class GuiCreateScreenshot extends GuiRenderSettings implements Loadable {
   private final ReplayMod mod;

   public GuiCreateScreenshot(ReplayMod mod) {
      super(GuiRenderSettings.createBaseScreen(), null, null);
      this.mod = mod;
      this.resetChildren(this.settingsList.getListPanel()).addElements(new VerticalLayout.Data(0.5D), (new GuiLabel()).setI18nText("replaymod.gui.advancedscreenshots.title"), this.mainPanel, new GuiPanel(), (new GuiLabel()).setI18nText("replaymod.gui.rendersettings.advanced"), this.advancedPanel, new GuiPanel());
      this.resetChildren(this.mainPanel).addElements(new GridLayout.Data(1.0D, 0.5D), (new GuiLabel()).setI18nText("replaymod.gui.rendersettings.renderer"), this.renderMethodDropdown, (new GuiLabel()).setI18nText("replaymod.gui.advancedscreenshots.resolution"), this.videoResolutionPanel, (new GuiLabel()).setI18nText("replaymod.gui.rendersettings.outputfile"), this.outputFileButton);
      this.resetChildren(this.advancedPanel).addElements(null, this.nametagCheckbox, this.alphaCheckbox, (new GuiPanel()).setLayout((new GridLayout()).setCellsEqualSize(false).setColumns(2).setSpacingX(5).setSpacingY(15)).addElements(new GridLayout.Data(0.0D, 0.5D), (new GuiLabel()).setI18nText("replaymod.gui.rendersettings.stabilizecamera"), this.stabilizePanel, this.chromaKeyingCheckbox, this.chromaKeyingColor));
      this.exportArguments.setText("");
      this.buttonPanel.removeElement(this.queueButton);
      this.renderButton.setI18nLabel("replaymod.gui.advancedscreenshots.create", new Object[0]).onClick(() -> {
         this.close();
         mod.runLater(() -> {
            try {
               RenderSettings settings = this.save(false);
               boolean success = (new ScreenshotRenderer(settings)).renderScreenshot();
               if (success) {
                  (new GuiUploadScreenshot(ReplayModReplay.instance.getReplayHandler().getOverlay(), mod, settings)).open();
               }
            } catch (Throwable var4) {
               Utils.error(ReplayModRender.LOGGER, this, CrashReport.forThrowable(var4, "Rendering video"), () -> {
               });
               this.getScreen().display();
            }

         });
      });
   }

   private <T extends GuiContainer<?>> T resetChildren(T container) {
      ArrayList<T> var10000 = new ArrayList(container.getChildren());
      Objects.requireNonNull(container);
      var10000.forEach(container::removeElement);
      return container;
   }

   public void open() {
      super.open();
      this.getScreen().display();
   }

   public void close() {
      super.close();
      this.getMinecraft().setScreen(null);
   }

   public void load() {
      ReplayModReplay.instance.getReplayHandler().getReplaySender().setReplaySpeed(0.0D);
   }

   protected File generateOutputFile(RenderSettings.EncodingPreset encodingPreset) {
      DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
      File screenshotFolder = new File(this.getMinecraft().gameDirectory, "screenshots");
      screenshotFolder.mkdirs();
      String baseName = DATE_FORMAT.format(new Date());
      int i = 1;

      while(true) {
         File screenshotFile = new File(screenshotFolder, baseName + (i == 1 ? "" : "_" + i) + ".png");
         if (!screenshotFile.exists()) {
            return screenshotFile;
         }

         ++i;
      }
   }

   public void load(RenderSettings settings) {
      super.load(settings.withEncodingPreset(RenderSettings.EncodingPreset.PNG));
   }

   protected Path getSettingsPath() {
      return this.getMinecraft().gameDirectory.toPath().resolve("config/replaymod-screenshotsettings.json");
   }
}
