package com.replaymod.simplepathing.gui;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiElement;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiNumberField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiClickable;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.IGuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiDropdownMenu;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Typeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.GridLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Utils;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import com.replaymod.pathing.properties.CameraProperties;
import com.replaymod.pathing.properties.TimestampProperty;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replaystudio.pathing.change.Change;
import com.replaymod.replaystudio.pathing.change.CombinedChange;
import com.replaymod.replaystudio.pathing.interpolation.CatmullRomSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.CubicSplineInterpolator;
import com.replaymod.replaystudio.pathing.interpolation.Interpolator;
import com.replaymod.replaystudio.pathing.interpolation.LinearInterpolator;
import com.replaymod.replaystudio.pathing.path.Keyframe;
import com.replaymod.replaystudio.pathing.path.Path;
import com.replaymod.replaystudio.pathing.path.PathSegment;
import com.replaymod.simplepathing.InterpolatorType;
import com.replaymod.simplepathing.SPTimeline;
import com.replaymod.simplepathing.Setting;
import com.replaymod.simplepathing.properties.ExplicitInterpolationProperty;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.client.resources.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GuiEditKeyframe<T extends GuiEditKeyframe<T>> extends AbstractGuiPopup<T> implements Typeable {
   protected static final Logger logger = LogManager.getLogger();
   protected final GuiPathing guiPathing;
   protected final long time;
   protected final Keyframe keyframe;
   protected final Path path;
   public final GuiLabel title = new GuiLabel();
   public final GuiPanel inputs = new GuiPanel();
   public final GuiNumberField timeMinField = newGuiNumberField().setSize(30, 20).setMinValue(0);
   public final GuiNumberField timeSecField = newGuiNumberField().setSize(20, 20).setMinValue(0).setMaxValue(59);
   public final GuiNumberField timeMSecField = newGuiNumberField().setSize(30, 20).setMinValue(0).setMaxValue(999);
   public final GuiPanel timePanel;
   public final GuiButton saveButton;
   public final GuiButton cancelButton;
   public final GuiPanel buttons;

   private static GuiNumberField newGuiNumberField() {
      return new GuiNumberField().setPrecision(0).setValidateOnFocusChange(true);
   }

   public GuiEditKeyframe(GuiPathing gui, SPTimeline.SPPath path, long time, String type) {
      super(ReplayModReplay.instance.getReplayHandler().getOverlay());
      this.timePanel = new GuiPanel().setLayout(new HorizontalLayout(HorizontalLayout.Alignment.RIGHT).setSpacing(3)).addElements(new HorizontalLayout.Data(0.5D), new GuiLabel().setI18nText("replaymod.gui.editkeyframe.timelineposition"), this.timeMinField, new GuiLabel().setI18nText("replaymod.gui.minutes"), this.timeSecField, new GuiLabel().setI18nText("replaymod.gui.seconds"), this.timeMSecField, new GuiLabel().setI18nText("replaymod.gui.milliseconds"));
      this.saveButton = new GuiButton().setSize(150, 20).setI18nLabel("replaymod.gui.save");
      this.cancelButton = new GuiButton().onClick(this::close).setSize(150, 20).setI18nLabel("replaymod.gui.cancel");
      this.buttons = new GuiPanel().setLayout(new HorizontalLayout(HorizontalLayout.Alignment.CENTER).setSpacing(7)).addElements(new HorizontalLayout.Data(0.5D), this.saveButton, this.cancelButton);
      this.setBackgroundColor(Colors.DARK_TRANSPARENT);
      this.popup.setLayout(new VerticalLayout().setSpacing(10)).addElements(new VerticalLayout.Data(0.5D, false), this.title, this.inputs, this.timePanel, this.buttons);
      this.guiPathing = gui;
      this.time = time;
      this.path = gui.getMod().getCurrentTimeline().getPath(path);
      this.keyframe = this.path.getKeyframe(time);
      Consumer<String> updateSaveButtonState = s -> {
         this.saveButton.setEnabled(this.canSave());
      };
      this.timeMinField.setValue((double)(time / 1000L / 60L)).onTextChanged(updateSaveButtonState);
      this.timeSecField.setValue((double)(time / 1000L % 60L)).onTextChanged(updateSaveButtonState);
      this.timeMSecField.setValue((double)(time % 1000L)).onTextChanged(updateSaveButtonState);
      this.title.setI18nText("replaymod.gui.editkeyframe.title." + type);
      this.saveButton.onClick(() -> {
         Change change = this.save();
         long newTime = (this.timeMinField.getInteger() * 60L + this.timeSecField.getInteger()) * 1000 + this.timeMSecField.getInteger();
         if (newTime != time) {
            change = CombinedChange.createFromApplied(change, gui.getMod().getCurrentTimeline().moveKeyframe(path, time, newTime));
            if (gui.getMod().getSelectedPath() == path && gui.getMod().getSelectedTime() == time) {
               gui.getMod().setSelected(path, newTime);
            }
         }

         gui.getMod().getCurrentTimeline().getTimeline().pushChange(change);
         this.close();
      });
   }

   private boolean canSave() {
      long newTime = (this.timeMinField.getInteger() * 60L + this.timeSecField.getInteger()) * 1000 + this.timeMSecField.getInteger();
      if (newTime >= 0L && newTime <= (long)this.guiPathing.timeline.getLength()) {
         return newTime == this.keyframe.getTime() || this.path.getKeyframe(newTime) == null;
      } else {
         return false;
      }
   }

   public boolean typeKey(ReadablePoint mousePosition, int keyCode, char keyChar, boolean ctrlDown, boolean shiftDown) {
      if (keyCode == 256) {
         this.cancelButton.onClick();
         return true;
      } else {
         return false;
      }
   }

   public void open() {
      super.open();
   }

   protected abstract Change save();

   public static class Position extends GuiEditKeyframe<GuiEditKeyframe.Position> {
      public final GuiNumberField xField = GuiEditKeyframe.newGuiNumberField().setSize(60, 20).setPrecision(5);
      public final GuiNumberField yField = GuiEditKeyframe.newGuiNumberField().setSize(60, 20).setPrecision(5);
      public final GuiNumberField zField = GuiEditKeyframe.newGuiNumberField().setSize(60, 20).setPrecision(5);
      public final GuiNumberField yawField = GuiEditKeyframe.newGuiNumberField().setSize(60, 20).setPrecision(5);
      public final GuiNumberField pitchField = GuiEditKeyframe.newGuiNumberField().setSize(60, 20).setPrecision(5);
      public final GuiNumberField rollField = GuiEditKeyframe.newGuiNumberField().setSize(60, 20).setPrecision(5);
      public final GuiEditKeyframe.Position.InterpolationPanel interpolationPanel = new GuiEditKeyframe.Position.InterpolationPanel();

      public Position(GuiPathing gui, SPTimeline.SPPath path, long keyframe) {
         super(gui, path, keyframe, "pos");
         GuiPanel positionInputs = new GuiPanel().setLayout(new GridLayout().setCellsEqualSize(false).setColumns(4).setSpacingX(3).setSpacingY(5)).addElements(new GridLayout.Data(1.0D, 0.5D), new GuiLabel().setI18nText("replaymod.gui.editkeyframe.xpos"), this.xField, new GuiLabel().setI18nText("replaymod.gui.editkeyframe.camyaw"), this.yawField, new GuiLabel().setI18nText("replaymod.gui.editkeyframe.ypos"), this.yField, new GuiLabel().setI18nText("replaymod.gui.editkeyframe.campitch"), this.pitchField, new GuiLabel().setI18nText("replaymod.gui.editkeyframe.zpos"), this.zField, new GuiLabel().setI18nText("replaymod.gui.editkeyframe.camroll"), this.rollField);
         this.inputs.setLayout(new VerticalLayout().setSpacing(10)).addElements(new VerticalLayout.Data(0.5D, false), positionInputs, this.interpolationPanel);
         this.keyframe.getValue(CameraProperties.POSITION).ifPresent(pos -> {
            this.xField.setValue(pos.getLeft());
            this.yField.setValue(pos.getMiddle());
            this.zField.setValue(pos.getRight());
         });
         this.keyframe.getValue(CameraProperties.ROTATION).ifPresent(rot -> {
            this.yawField.setValue((double) rot.getLeft());
            this.pitchField.setValue((double) rot.getMiddle());
            this.rollField.setValue((double) rot.getRight());
         });
         Utils.link(this.xField, this.yField, this.zField, this.yawField, this.pitchField, this.rollField, this.timeMinField, this.timeSecField, this.timeMSecField);
         this.popup.invokeAll(IGuiLabel.class, e -> {
            e.setColor(Colors.BLACK);
         });
      }

      protected Change save() {
         SPTimeline timeline = this.guiPathing.getMod().getCurrentTimeline();
         Change positionChange = timeline.updatePositionKeyframe(this.time, this.xField.getDouble(), this.yField.getDouble(), this.zField.getDouble(), this.yawField.getFloat(), this.pitchField.getFloat(), this.rollField.getFloat());
         if (this.interpolationPanel.getSettingsPanel() == null) {
            return positionChange;
         } else {
            Interpolator interpolator = this.interpolationPanel.getSettingsPanel().createInterpolator();
            return this.interpolationPanel.getInterpolatorType() == InterpolatorType.DEFAULT ? CombinedChange.createFromApplied(positionChange, timeline.setInterpolatorToDefault(this.time), timeline.setDefaultInterpolator(interpolator)) : CombinedChange.createFromApplied(positionChange, timeline.setInterpolator(this.time, interpolator));
         }
      }

      protected GuiEditKeyframe.Position getThis() {
         return this;
      }

      public class InterpolationPanel extends AbstractGuiContainer<GuiEditKeyframe.Position.InterpolationPanel> {
         private GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel settingsPanel;
         private GuiDropdownMenu<InterpolatorType> dropdown;

         public InterpolationPanel() {
            this.setLayout(new VerticalLayout());
            this.dropdown = new GuiDropdownMenu<InterpolatorType>().setToString(s -> {
               return I18n.get(s.getI18nName());
            }).setValues(InterpolatorType.values()).setHeight(20).onSelection(i -> {
               this.setSettingsPanel(this.dropdown.getSelectedValue());
            });

            for (Entry<InterpolatorType, IGuiClickable> interpolatorTypeIGuiClickableEntry : this.dropdown.getDropdownEntries().entrySet()) {
               interpolatorTypeIGuiClickableEntry.getValue().setTooltip(new GuiTooltip().setI18nText(interpolatorTypeIGuiClickableEntry.getKey().getI18nDescription()));
            }

            GuiPanel dropdownPanel = new GuiPanel().setLayout(new GridLayout().setCellsEqualSize(false).setColumns(2).setSpacingX(3).setSpacingY(5)).addElements(new GridLayout.Data(1.0D, 0.5D), new GuiLabel().setI18nText("replaymod.gui.editkeyframe.interpolator"), this.dropdown);
            this.addElements(new VerticalLayout.Data(0.5D, false), dropdownPanel);
            Optional<PathSegment> segment = Position.this.path.getSegments().stream().filter(s -> {
               return s.getStartKeyframe() == Position.this.keyframe;
            }).findFirst();
            if (segment.isPresent()) {
               Interpolator interpolator = segment.get().getInterpolator();
               InterpolatorType type = InterpolatorType.fromClass(interpolator.getClass());
               if (Position.this.keyframe.getValue(ExplicitInterpolationProperty.PROPERTY).isPresent()) {
                  this.dropdown.setSelected(type);
               } else {
                  this.setSettingsPanel(InterpolatorType.DEFAULT);
                  type = InterpolatorType.DEFAULT;
               }

               if (this.getInterpolatorTypeNoDefault(type).getInterpolatorClass().isInstance(interpolator)) {
                  this.settingsPanel.loadSettings(interpolator);
               }
            } else {
               this.dropdown.setDisabled();
            }

         }

         public GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel getSettingsPanel() {
            return this.settingsPanel;
         }

         public void setSettingsPanel(InterpolatorType type) {
            this.removeElement(this.settingsPanel);
            switch(this.getInterpolatorTypeNoDefault(type)) {
            case CATMULL_ROM:
               this.settingsPanel = new GuiEditKeyframe.Position.InterpolationPanel.CatmullRomSettingsPanel();
               break;
            case CUBIC:
               this.settingsPanel = new GuiEditKeyframe.Position.InterpolationPanel.CubicSettingsPanel();
               break;
            case LINEAR:
               this.settingsPanel = new GuiEditKeyframe.Position.InterpolationPanel.LinearSettingsPanel();
            }

            this.addElements(new GridLayout.Data(0.5D, 0.5D), this.settingsPanel);
         }

         protected InterpolatorType getInterpolatorTypeNoDefault(InterpolatorType interpolatorType) {
            if (interpolatorType != InterpolatorType.DEFAULT && interpolatorType != null) {
               return interpolatorType;
            } else {
               InterpolatorType defaultType = InterpolatorType.fromString(Position.this.guiPathing.getMod().getCore().getSettingsRegistry().get(Setting.DEFAULT_INTERPOLATION));
               return defaultType;
            }
         }

         public InterpolatorType getInterpolatorType() {
            return this.dropdown.getSelectedValue();
         }

         protected GuiEditKeyframe.Position.InterpolationPanel getThis() {
            return this;
         }

         public abstract class SettingsPanel<I extends Interpolator, T extends GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel<I, T>> extends AbstractGuiContainer<T> {
            public abstract void loadSettings(I var1);

            public abstract I createInterpolator();
         }

         public class CatmullRomSettingsPanel extends GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel<CatmullRomSplineInterpolator, GuiEditKeyframe.Position.InterpolationPanel.CatmullRomSettingsPanel> {
            public final GuiLabel alphaLabel;
            public final GuiNumberField alphaField;

            public CatmullRomSettingsPanel() {
               super();
               this.alphaLabel = new GuiLabel().setColor(Colors.BLACK).setI18nText("replaymod.gui.editkeyframe.interpolator.catmullrom.alpha");
               this.alphaField = new GuiNumberField().setSize(100, 20).setPrecision(5).setMinValue(0).setValidateOnFocusChange(true);
               this.setLayout(new HorizontalLayout(HorizontalLayout.Alignment.CENTER));
               this.addElements(new HorizontalLayout.Data(0.5D), this.alphaLabel, this.alphaField);
            }

            public void loadSettings(CatmullRomSplineInterpolator interpolator) {
               this.alphaField.setValue(interpolator.getAlpha());
            }

            public CatmullRomSplineInterpolator createInterpolator() {
               return new CatmullRomSplineInterpolator(this.alphaField.getDouble());
            }

            protected GuiEditKeyframe.Position.InterpolationPanel.CatmullRomSettingsPanel getThis() {
               return this;
            }
         }

         public class CubicSettingsPanel extends GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel<CubicSplineInterpolator, GuiEditKeyframe.Position.InterpolationPanel.CubicSettingsPanel> {
            public CubicSettingsPanel() {
               super();
            }

            public void loadSettings(CubicSplineInterpolator interpolator) {
            }

            public CubicSplineInterpolator createInterpolator() {
               return new CubicSplineInterpolator();
            }

            protected GuiEditKeyframe.Position.InterpolationPanel.CubicSettingsPanel getThis() {
               return this;
            }
         }

         public class LinearSettingsPanel extends GuiEditKeyframe.Position.InterpolationPanel.SettingsPanel<LinearInterpolator, GuiEditKeyframe.Position.InterpolationPanel.LinearSettingsPanel> {
            public LinearSettingsPanel() {
               super();
            }

            public void loadSettings(LinearInterpolator interpolator) {
            }

            public LinearInterpolator createInterpolator() {
               return new LinearInterpolator();
            }

            protected GuiEditKeyframe.Position.InterpolationPanel.LinearSettingsPanel getThis() {
               return this;
            }
         }
      }
   }

   public static class Time extends GuiEditKeyframe<GuiEditKeyframe.Time> {
      public final GuiNumberField timestampMinField = GuiEditKeyframe.newGuiNumberField().setSize(30, 20).setMinValue(0);
      public final GuiNumberField timestampSecField = GuiEditKeyframe.newGuiNumberField().setSize(20, 20).setMinValue(0).setMaxValue(59);
      public final GuiNumberField timestampMSecField = GuiEditKeyframe.newGuiNumberField().setSize(30, 20).setMinValue(0).setMaxValue(999);

      public Time(GuiPathing gui, SPTimeline.SPPath path, long keyframe) {
         super(gui, path, keyframe, "time");
         this.inputs.setLayout(new HorizontalLayout(HorizontalLayout.Alignment.RIGHT).setSpacing(3)).addElements(new HorizontalLayout.Data(0.5D), new GuiLabel().setI18nText("replaymod.gui.editkeyframe.timestamp"), this.timestampMinField, new GuiLabel().setI18nText("replaymod.gui.minutes"), this.timestampSecField, new GuiLabel().setI18nText("replaymod.gui.seconds"), this.timestampMSecField, new GuiLabel().setI18nText("replaymod.gui.milliseconds"));
         this.keyframe.getValue(TimestampProperty.PROPERTY).ifPresent(time -> {
            this.timestampMinField.setValue(time / 1000 / 60);
            this.timestampSecField.setValue(time / 1000 % 60);
            this.timestampMSecField.setValue(time % 1000);
         });
         Utils.link(this.timestampMinField, this.timestampSecField, this.timestampMSecField, this.timeMinField, this.timeSecField, this.timeMSecField);
         this.popup.invokeAll(IGuiLabel.class, e -> {
            e.setColor(Colors.BLACK);
         });
      }

      protected Change save() {
         int time = (this.timestampMinField.getInteger() * 60 + this.timestampSecField.getInteger()) * 1000 + this.timestampMSecField.getInteger();
         return this.guiPathing.getMod().getCurrentTimeline().updateTimeKeyframe(this.keyframe.getTime(), time);
      }

      protected GuiEditKeyframe.Time getThis() {
         return this;
      }
   }

   public static class Spectator extends GuiEditKeyframe<GuiEditKeyframe.Spectator> {
      public Spectator(GuiPathing gui, SPTimeline.SPPath path, long keyframe) {
         super(gui, path, keyframe, "spec");
         Utils.link(this.timeMinField, this.timeSecField, this.timeMSecField);
         this.popup.invokeAll(IGuiLabel.class, e -> {
            e.setColor(Colors.BLACK);
         });
      }

      protected Change save() {
         return CombinedChange.createFromApplied();
      }

      protected GuiEditKeyframe.Spectator getThis() {
         return this;
      }
   }
}
