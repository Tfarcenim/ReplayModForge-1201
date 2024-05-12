package com.replaymod.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.replaymod.core.events.KeyBindingEventCallback;
import com.replaymod.core.events.KeyEventCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.versions.MCVer;
import com.replaymod.mixin.KeyBindingAccessor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;

public class KeyBindingRegistry extends EventRegistrations {
   private static final String CATEGORY = "replaymod.title";
   private final Map<String, KeyBindingRegistry.Binding> bindings = new HashMap<>();
   private final Set<KeyMapping> onlyInReplay = new HashSet<>();
   private final Multimap<Integer, Supplier<Boolean>> rawHandlers = ArrayListMultimap.create();

   public KeyBindingRegistry() {
      this.on(PreRenderCallback.EVENT, this::handleRepeatedKeyBindings);
      this.on(KeyBindingEventCallback.EVENT, this::handleKeyBindings);
      this.on(KeyEventCallback.EVENT, (keyCode, scanCode, action, modifiers) -> {
         return this.handleRaw(keyCode, action);
      });
   }

   public KeyBindingRegistry.Binding registerKeyBinding(String name, int keyCode, Runnable whenPressed, boolean onlyInRepay) {
      KeyBindingRegistry.Binding binding = this.registerKeyBinding(name, keyCode, onlyInRepay);
      binding.handlers.add(whenPressed);
      return binding;
   }

   public KeyBindingRegistry.Binding registerRepeatedKeyBinding(String name, int keyCode, Runnable whenPressed, boolean onlyInRepay) {
      KeyBindingRegistry.Binding binding = this.registerKeyBinding(name, keyCode, onlyInRepay);
      binding.repeatedHandlers.add(whenPressed);
      return binding;
   }

   private KeyBindingRegistry.Binding registerKeyBinding(String name, int keyCode, boolean onlyInRepay) {
      KeyBindingRegistry.Binding binding = this.bindings.get(name);
      if (binding == null) {
         if (keyCode == 0) {
            keyCode = -1;
         }

         ResourceLocation id = new ResourceLocation("replaymod", name.substring("key.replaymod.".length()));
         String key = String.format("key.%s.%s", id.getNamespace(), id.getPath());
         KeyMapping keyBinding = new KeyMapping(key, Type.KEYSYM, keyCode, "replaymod.title");
         //KeyBindingHelper.registerKeyBinding(keyBinding);
         MCVer.getMinecraft().options.keyMappings = ArrayUtils.add(MCVer.getMinecraft().options.keyMappings,
                 keyBinding);
         binding = new KeyBindingRegistry.Binding(name, keyBinding);
         this.bindings.put(name, binding);
         if (onlyInRepay) {
            this.onlyInReplay.add(keyBinding);
         }
      } else if (!onlyInRepay) {
         this.onlyInReplay.remove(binding.keyBinding);
      }

      return binding;
   }

   public void registerRaw(int keyCode, Supplier<Boolean> whenPressed) {
      this.rawHandlers.put(keyCode, whenPressed);
   }

   public Map<String, KeyBindingRegistry.Binding> getBindings() {
      return Collections.unmodifiableMap(this.bindings);
   }

   public Set<KeyMapping> getOnlyInReplay() {
      return Collections.unmodifiableSet(this.onlyInReplay);
   }

   public void handleRepeatedKeyBindings() {
      Iterator var1 = this.bindings.values().iterator();

      while(var1.hasNext()) {
         KeyBindingRegistry.Binding binding = (KeyBindingRegistry.Binding)var1.next();
         if (binding.keyBinding.isDown()) {
            this.invokeKeyBindingHandlers(binding, binding.repeatedHandlers);
         }
      }

   }

   private void handleKeyBindings() {
      Iterator var1 = this.bindings.values().iterator();

      while(var1.hasNext()) {
         KeyBindingRegistry.Binding binding = (KeyBindingRegistry.Binding)var1.next();

         while(binding.keyBinding.consumeClick()) {
            this.invokeKeyBindingHandlers(binding, binding.handlers);
            this.invokeKeyBindingHandlers(binding, binding.repeatedHandlers);
         }
      }

   }

   private void invokeKeyBindingHandlers(KeyBindingRegistry.Binding binding, Collection<Runnable> handlers) {
      Iterator var3 = handlers.iterator();

      while(var3.hasNext()) {
         Runnable runnable = (Runnable)var3.next();

         try {
            runnable.run();
         } catch (Throwable var8) {
            CrashReport crashReport = CrashReport.forThrowable(var8, "Handling Key Binding");
            CrashReportCategory category = crashReport.addCategory("Key Binding");
            category.setDetail("Key Binding", () -> {
               return binding.name;
            });
            Objects.requireNonNull(runnable);
            category.setDetail("Handler", runnable::toString);
            throw new ReportedException(crashReport);
         }
      }

   }

   private boolean handleRaw(int keyCode, int action) {
      if (action != KeyEventCallback.ACTION_PRESS)
         return false;
      for (final Supplier<Boolean> handler : rawHandlers.get(keyCode)) {
         try {
            if (handler.get()) {
               return true;
            }
         } catch (Throwable cause) {
            CrashReport crashReport = CrashReport.forThrowable(cause, "Handling Raw Key Binding");
            CrashReportCategory category = crashReport.addCategory("Key Binding");
            category.setDetail("Key Code", () -> String.valueOf(keyCode));
            category.setDetail("Handler", handler::toString);
            throw new ReportedException(crashReport);
         }
      }
      return false;
   }

   public class Binding {
      public final String name;
      public final KeyMapping keyBinding;
      private final List<Runnable> handlers = new ArrayList();
      private final List<Runnable> repeatedHandlers = new ArrayList();
      private boolean autoActivation;
      private Consumer<Boolean> autoActivationUpdate;

      public Binding(String name, KeyMapping keyBinding) {
         this.name = name;
         this.keyBinding = keyBinding;
      }

      public String getBoundKey() {
         try {
            return this.keyBinding.getTranslatedKeyMessage().getString();
         } catch (ArrayIndexOutOfBoundsException var2) {
            return "Unknown";
         }
      }

      public boolean isBound() {
         return !this.keyBinding.isUnbound();
      }

      public void trigger() {
         KeyBindingAccessor acc = (KeyBindingAccessor)this.keyBinding;
         acc.setPressTime(acc.getPressTime() + 1);
         KeyBindingRegistry.this.handleKeyBindings();
      }

      public void registerAutoActivationSupport(boolean active, Consumer<Boolean> update) {
         this.autoActivation = active;
         this.autoActivationUpdate = update;
      }

      public boolean supportsAutoActivation() {
         return this.autoActivationUpdate != null;
      }

      public boolean isAutoActivating() {
         return this.supportsAutoActivation() && this.autoActivation;
      }

      public void setAutoActivating(boolean active) {
         if (this.autoActivation != active) {
            this.autoActivation = active;
            this.autoActivationUpdate.accept(active);
         }
      }
   }
}
