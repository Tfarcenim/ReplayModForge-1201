package com.replaymod.replay.camera;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.replaymod.replay.Setting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CameraControllerRegistry {
   private final Map<String, Function<CameraEntity, CameraController>> constructors = new ConcurrentHashMap();

   public void register(String name, Function<CameraEntity, CameraController> constructor) {
      Preconditions.checkState(!this.constructors.containsKey(name), "Controller " + name + " is already registered.");
      this.constructors.put(name, constructor);
      Setting.CAMERA.setChoices(new ArrayList(this.getControllers()));
   }

   public Set<String> getControllers() {
      return Collections.unmodifiableSet(this.constructors.keySet());
   }

   public CameraController create(String name, CameraEntity cameraEntity) {
      return (CameraController)((Function)this.constructors.get(name)).apply(cameraEntity);
   }
}
