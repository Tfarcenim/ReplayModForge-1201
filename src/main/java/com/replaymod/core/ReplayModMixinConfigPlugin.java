package com.replaymod.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class ReplayModMixinConfigPlugin implements IMixinConfigPlugin {
   private final Logger logger = LogManager.getLogger("replaymod/mixin");
   private final boolean hasIris = false;//ModList.get().isLoaded("iris");

   static boolean hasClass(String name) throws IOException {
      InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name.replace('.', '/') + ".class");
      if (stream != null) {
         stream.close();
      }

      return stream != null;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      if (mixinClassName.endsWith("_OF")) {
         return false;
      } else if (mixinClassName.endsWith("_NoOF")) {
         return true;
      } else {
         return !mixinClassName.endsWith("_Iris") || this.hasIris;
      }
   }

   public void onLoad(String mixinPackage) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   public ReplayModMixinConfigPlugin() throws IOException {
   }
}
