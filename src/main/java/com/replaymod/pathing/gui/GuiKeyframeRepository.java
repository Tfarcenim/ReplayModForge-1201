package com.replaymod.pathing.gui;

import com.google.common.util.concurrent.SettableFuture;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.GuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.RenderInfo;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.*;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTextField;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Closeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.GridLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.GuiYesNoPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import com.replaymod.render.gui.GuiRenderQueue;
import com.replaymod.render.gui.GuiRenderSettings;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.serialize.TimelineSerialization;
import com.replaymod.replaystudio.replay.ReplayFile;
import net.minecraft.CrashReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GuiKeyframeRepository extends GuiScreen implements Closeable, Typeable {
   private static final Logger LOGGER = LogManager.getLogger();
   public final GuiPanel contentPanel;
   public final GuiLabel title;
   public final GuiVerticalList list;
   public final GuiButton overwriteButton;
   public final GuiButton saveAsButton;
   public final GuiButton loadButton;
   public final GuiButton renameButton;
   public final GuiButton removeButton;
   public final GuiButton copyButton;
   public final GuiButton pasteButton;
   public final GuiButton addToQueueButton;
   public final GuiPanel buttonPanel;
   private final Map<String, Timeline> timelines;
   private final Timeline currentTimeline;
   private final SettableFuture<Timeline> future;
   private final PathingRegistry registry;
   private final ReplayFile replayFile;
   private final Set<GuiKeyframeRepository.Entry> selectedEntries;

   public GuiKeyframeRepository(PathingRegistry registry, ReplayFile replayFile, Timeline currentTimeline) throws IOException {
      this.contentPanel = new GuiPanel(this).setBackgroundColor(Colors.DARK_TRANSPARENT);
      this.title = new GuiLabel(this.contentPanel).setI18nText("replaymod.gui.keyframerepository.title");
      this.list = new GuiVerticalList(this.contentPanel).setDrawShadow(true).setDrawSlider(true);
      this.overwriteButton = new GuiButton().onClick(new Runnable() {
         public void run() {
            GuiYesNoPopup.open(GuiKeyframeRepository.this, new GuiLabel().setI18nText("replaymod.gui.keyframerepo.overwrite").setColor(Colors.BLACK)).setYesI18nLabel("gui.yes").setNoI18nLabel("gui.no").onAccept(() -> {

               for (Entry entry : GuiKeyframeRepository.this.selectedEntries) {
                  GuiKeyframeRepository.this.timelines.put(entry.name, GuiKeyframeRepository.this.currentTimeline);
               }

               GuiKeyframeRepository.this.overwriteButton.setDisabled();
               GuiKeyframeRepository.this.save();
            });
         }
      }).setSize(75, 20).setI18nLabel("replaymod.gui.overwrite", new Object[0]).setDisabled();
      this.saveAsButton = new GuiButton().onClick(new Runnable() {
         @Override
         public void run() {
            final GuiTextField nameField = new GuiTextField().setSize(200, 20).setFocused(true);
            final GuiYesNoPopup popup = GuiYesNoPopup
                    .open(GuiKeyframeRepository.this,
                            new GuiLabel().setI18nText("replaymod.gui.saveas").setColor(Colors.BLACK), nameField)
                    .setYesI18nLabel("replaymod.gui.save").setNoI18nLabel("replaymod.gui.cancel");
            popup.getYesButton().setDisabled();
            ((VerticalLayout) popup.getInfo().getLayout()).setSpacing(7);
            nameField.onEnter(() -> {
               if (popup.getYesButton().isEnabled()) {
                  popup.getYesButton().onClick();
               }
            }).onTextChanged(obj -> popup.getYesButton().setEnabled(
                    !nameField.getText().isEmpty() && !timelines.containsKey(nameField.getText())));
            popup.onAccept(() -> {
               String name = nameField.getText();
               timelines.put(name, currentTimeline);
               list.getListPanel().addElements(null, new Entry(name));
               save();
            });
         }
      }).setSize(75, 20).setI18nLabel("replaymod.gui.saveas");
      this.loadButton = new GuiButton().onClick(new Runnable() {
         public void run() {
            GuiKeyframeRepository.this.getMinecraft().setScreen(null);

            try {
               Timeline timeline = GuiKeyframeRepository.this.timelines.get(GuiKeyframeRepository.this.selectedEntries.iterator().next().name);

               for (Path path : timeline.getPaths()) {
                  path.updateAll();
               }

               GuiKeyframeRepository.this.future.set(timeline);
            } catch (Throwable var4) {
               GuiKeyframeRepository.this.future.setException(var4);
            }

         }
      }).setSize(75, 20).setI18nLabel("replaymod.gui.load", new Object[0]).setDisabled();
      this.renameButton = new GuiButton().onClick(new Runnable() {
         @Override
         public void run() {
            Entry selectedEntry = selectedEntries.iterator().next();
            final GuiTextField nameField = new GuiTextField().setSize(200, 20).setFocused(true)
                    .setText(selectedEntry.name);
            final GuiYesNoPopup popup = GuiYesNoPopup
                    .open(GuiKeyframeRepository.this,
                            new GuiLabel().setI18nText("replaymod.gui.rename").setColor(Colors.BLACK), nameField)
                    .setYesI18nLabel("replaymod.gui.done").setNoI18nLabel("replaymod.gui.cancel");
            popup.getYesButton().setDisabled();
            ((VerticalLayout) popup.getInfo().getLayout()).setSpacing(7);
            nameField.onEnter(() -> {
               if (popup.getYesButton().isEnabled()) {
                  popup.getYesButton().onClick();
               }
            }).onTextChanged(obj -> popup.getYesButton().setEnabled(
                    !nameField.getText().isEmpty() && !timelines.containsKey(nameField.getText())));
            popup.onAccept(() -> {
               String name = nameField.getText();
               timelines.put(name, timelines.remove(selectedEntry.name));
               selectedEntry.name = name;
               selectedEntry.label.setText(name);
               save();
            });
         }
      }).setSize(75, 20).setI18nLabel("replaymod.gui.rename").setDisabled();
      this.removeButton = new GuiButton().onClick(new Runnable() {
         public void run() {
            GuiYesNoPopup.open(GuiKeyframeRepository.this, new GuiLabel().setI18nText("replaymod.gui.keyframerepo.delete", new Object[0]).setColor(Colors.BLACK)).setYesI18nLabel("replaymod.gui.delete").setNoI18nLabel("replaymod.gui.cancel").onAccept(() -> {

               for (Entry entry : GuiKeyframeRepository.this.selectedEntries) {
                  GuiKeyframeRepository.this.timelines.remove(entry.name);
                  GuiKeyframeRepository.this.list.getListPanel().removeElement(entry);
               }

               GuiKeyframeRepository.this.selectedEntries.clear();
               GuiKeyframeRepository.this.updateButtons();
               GuiKeyframeRepository.this.save();
            });
         }
      }).setSize(75, 20).setI18nLabel("replaymod.gui.remove", new Object[0]).setDisabled();
      this.copyButton = new GuiButton().onClick(new Runnable() {
         public void run() {
            Map<String, Timeline> toBeSerialized = new HashMap<>();

            for (Entry entry : GuiKeyframeRepository.this.selectedEntries) {
               toBeSerialized.put(entry.name, GuiKeyframeRepository.this.timelines.get(entry.name));
            }

            try {
               TimelineSerialization serialization = new TimelineSerialization(GuiKeyframeRepository.this.registry, null);
               MCVer.setClipboardString(serialization.serialize(toBeSerialized));
            } catch (Throwable var4) {
               var4.printStackTrace();
               CrashReport report = CrashReport.forThrowable(var4, "Copying timeline(s)");
               Utils.error(GuiKeyframeRepository.LOGGER, GuiKeyframeRepository.this, report, () -> {
               });
            }

         }
      }).setSize(75, 20).setI18nLabel("replaymod.gui.copy", new Object[0]).setDisabled();
      this.pasteButton = new GuiButton().onClick(new Runnable() {
         public void run() {
            try {
               TimelineSerialization serialization = new TimelineSerialization(GuiKeyframeRepository.this.registry, null);

               for (Map.Entry<String, Timeline> stringTimelineEntry : serialization.deserialize(MCVer.getClipboardString()).entrySet()) {

                  String name;
                  for (name = stringTimelineEntry.getKey(); GuiKeyframeRepository.this.timelines.containsKey(name); name = name + " (Copy)") {
                  }

                  GuiKeyframeRepository.this.timelines.put(name, stringTimelineEntry.getValue());
                  GuiKeyframeRepository.this.list.getListPanel().addElements(null, GuiKeyframeRepository.this.new Entry(name));
               }

               GuiKeyframeRepository.this.save();
            } catch (Throwable var5) {
               var5.printStackTrace();
            }

         }
      }).setSize(75, 20).setI18nLabel("replaymod.gui.paste");
      this.addToQueueButton = new GuiButton().onClick(new Runnable() {
         @Override
         public void run() {
            ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
            GuiRenderQueue queue = new GuiRenderQueue(GuiKeyframeRepository.this, replayHandler, null);
            queue.open();

            Set<String> selected = selectedEntries.stream().map(e -> e.name).collect(Collectors.toSet());
            ArrayDeque<Map.Entry<String, Timeline>> toBeAdded = new ArrayDeque<>();
            // Iterating over timelines to get consistent ordering (cause selectedEntries is
            // an unordered set)
            // and we need to get the Timelines anyway.
            for (Map.Entry<String, Timeline> entry : timelines.entrySet()) {
               if (selected.contains(entry.getKey())) {
                  toBeAdded.offerLast(entry);
               }
            }
            new Runnable() {
               @Override
               public void run() {
                  Map.Entry<String, Timeline> entry = toBeAdded.pollFirst();
                  if (entry == null) {
                     return;
                  }
                  String name = entry.getKey();
                  Timeline timeline = entry.getValue();
                  GuiRenderSettings settingsGui = queue.addJob(timeline);
                  settingsGui.buttonPanel.removeElement(settingsGui.renderButton);
                  settingsGui.setOutputFileBaseName(name);
                  Runnable orgOnClick = settingsGui.queueButton.getOnClick();
                  settingsGui.queueButton.onClick(() -> {
                     orgOnClick.run();
                     this.run();
                  });
                  settingsGui.open();
               }
            }.run();
         }
      }).setSize(75, 20).setI18nLabel("replaymod.gui.rendersettings.addtoqueue");
      this.buttonPanel = new GuiPanel(this.contentPanel).setLayout(new GridLayout().setColumns(4).setSpacingX(5).setSpacingY(5)).addElements(null, this.overwriteButton, this.saveAsButton, this.renameButton, this.removeButton, this.loadButton, this.addToQueueButton, this.copyButton, this.pasteButton);
      this.timelines = new LinkedHashMap<>();
      this.future = SettableFuture.create();
      this.selectedEntries = new HashSet<>();
      this.setBackground(AbstractGuiScreen.Background.NONE);
      this.setLayout(new CustomLayout<GuiScreen>() {
         protected void layout(GuiScreen container, int width, int height) {
            this.pos(GuiKeyframeRepository.this.contentPanel, width / 2 - this.width(GuiKeyframeRepository.this.contentPanel) / 2, height / 2 - this.height(GuiKeyframeRepository.this.contentPanel) / 2);
         }
      });
      this.contentPanel.setLayout(new CustomLayout<GuiPanel>() {
         protected void layout(GuiPanel container, int width, int height) {
            this.pos(GuiKeyframeRepository.this.title, width / 2 - this.width(GuiKeyframeRepository.this.title) / 2, 5);
            this.size(GuiKeyframeRepository.this.list, width, height - 10 - this.height(GuiKeyframeRepository.this.buttonPanel) - 10 - this.y(GuiKeyframeRepository.this.title) - this.height(GuiKeyframeRepository.this.title) - 5);
            this.pos(GuiKeyframeRepository.this.list, width / 2 - this.width(GuiKeyframeRepository.this.list) / 2, this.y(GuiKeyframeRepository.this.title) + this.height(GuiKeyframeRepository.this.title) + 5);
            this.pos(GuiKeyframeRepository.this.buttonPanel, width / 2 - this.width(GuiKeyframeRepository.this.buttonPanel) / 2, this.y(GuiKeyframeRepository.this.list) + this.height(GuiKeyframeRepository.this.list) + 10);
         }

         public ReadableDimension calcMinSize(GuiContainer<?> container) {
            ReadableDimension screenSize = GuiKeyframeRepository.this.getMinSize();
            return new Dimension(screenSize.getWidth() - 10, screenSize.getHeight() - 10);
         }
      });
      this.registry = registry;
      this.replayFile = replayFile;
      this.currentTimeline = currentTimeline;
      this.timelines.putAll(replayFile.getTimelines(registry));

      for (Map.Entry<String, Timeline> stringTimelineEntry : this.timelines.entrySet()) {
         if (!stringTimelineEntry.getKey().isEmpty()) {
            this.list.getListPanel().addElements(null, new Entry(stringTimelineEntry.getKey()));
         }
      }

      this.updateButtons();
   }

   private void updateButtons() {
      int selected = this.selectedEntries.size();
      this.overwriteButton.setEnabled(selected >= 1);
      this.loadButton.setEnabled(selected == 1);
      this.renameButton.setEnabled(selected == 1);
      this.removeButton.setEnabled(selected >= 1);
      this.copyButton.setEnabled(selected >= 1);
      this.addToQueueButton.setEnabled(selected >= 1);
   }

   public void display() {
      super.display();
      ReplayModReplay.instance.getReplayHandler().getOverlay().setVisible(false);
   }

   public void close() {
      ReplayModReplay.instance.getReplayHandler().getOverlay().setVisible(true);
   }

   public SettableFuture<Timeline> getFuture() {
      return this.future;
   }

   public void save() {
      try {
         this.replayFile.writeTimelines(this.registry, this.timelines);
      } catch (IOException var2) {
         var2.printStackTrace();
         ReplayMod.instance.printWarningToChat("Error saving timelines: " + var2.getMessage());
      }

   }

   public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
      if (com.replaymod.core.versions.MCVer.Keyboard.hasControlDown()) {
         switch(keyCode) {
         case 65:
            if (this.selectedEntries.size() < this.timelines.size()) {

               for (GuiElement<?> child : this.list.getListPanel().getChildren()) {
                  if (child instanceof Entry) {
                     this.selectedEntries.add((Entry) child);
                  }
               }
            } else {
               this.selectedEntries.clear();
            }

            this.updateButtons();
            return true;
         case 67:
            this.copyButton.onClick();
            return true;
         case 86:
            this.pasteButton.onClick();
            return true;
         }
      }

      return false;
   }

   public class Entry extends AbstractGuiClickableContainer<GuiKeyframeRepository.Entry> {
      public final GuiLabel label = new GuiLabel(this);
      private String name;

      public Entry(String name) {
         this.name = name;

         setLayout(new CustomLayout<Entry>() {
            @Override
            protected void layout(Entry container, int width, int height) {
               pos(label, 5, height / 2 - height(label) / 2);
            }

            @Override
            public ReadableDimension calcMinSize(GuiContainer<?> container) {
               return new Dimension(buttonPanel.calcMinSize().getWidth(), 16);
            }
         });
         label.setText(name);
      }

      protected void onClick() {
         if (!com.replaymod.core.versions.MCVer.Keyboard.hasControlDown()) {
            GuiKeyframeRepository.this.selectedEntries.clear();
         }

         if (GuiKeyframeRepository.this.selectedEntries.contains(this)) {
            GuiKeyframeRepository.this.selectedEntries.remove(this);
         } else {
            GuiKeyframeRepository.this.selectedEntries.add(this);
         }

         GuiKeyframeRepository.this.updateButtons();
      }

      public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
         if (GuiKeyframeRepository.this.selectedEntries.contains(this)) {
            renderer.drawRect(0, 0, size.getWidth(), size.getHeight(), Colors.BLACK);
            renderer.drawRect(0, 0, 2, size.getHeight(), Colors.WHITE);
         }

         super.draw(renderer, size, renderInfo);
      }

      protected GuiKeyframeRepository.Entry getThis() {
         return this;
      }
   }
}
