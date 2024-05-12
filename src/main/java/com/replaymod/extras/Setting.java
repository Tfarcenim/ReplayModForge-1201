package com.replaymod.extras;

import com.replaymod.core.SettingsRegistry;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Setting<T> {
   public static final SettingsRegistry.SettingKey<Boolean> ASK_FOR_OPEN_EYE = new SettingsRegistry.SettingKeys("advanced", "askForOpenEye", (String)null, true);
   public static final SettingsRegistry.SettingKey<Boolean> SKIP_POST_SCREENSHOT_GUI = new SettingsRegistry.SettingKeys("advanced", "skipPostScreenshotGui", (String)null, false);
   public static final SettingsRegistry.MultipleChoiceSettingKeys<String> FULL_BRIGHTNESS;

   static {
      FULL_BRIGHTNESS = new SettingsRegistry.MultipleChoiceSettingKeys("advanced", "fullBrightness", "replaymod.gui.settings.fullbrightness", FullBrightness.Type.Gamma.toString());
      FULL_BRIGHTNESS.setChoices((List)Arrays.stream(FullBrightness.Type.values()).map(Object::toString).collect(Collectors.toList()));
   }
}
