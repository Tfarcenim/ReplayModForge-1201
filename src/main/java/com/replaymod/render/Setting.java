package com.replaymod.render;

import com.replaymod.core.SettingsRegistry;

public final class Setting<T> {
   public static final SettingsRegistry.SettingKey<String> RENDER_PATH = new SettingsRegistry.SettingKeys("advanced", "renderPath", (String)null, "./replay_videos/");
   public static final SettingsRegistry.SettingKey<Boolean> SKIP_POST_RENDER_GUI = new SettingsRegistry.SettingKeys("advanced", "skipPostRenderGui", (String)null, false);
   public static final SettingsRegistry.SettingKey<Boolean> FRAME_TIME_FROM_WORLD_TIME = new SettingsRegistry.SettingKeys("render", "frameTimeFromWorldTime", (String)null, false);
}
