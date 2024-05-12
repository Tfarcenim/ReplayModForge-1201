package com.replaymod.core;

import com.replaymod.compat.ForgeEventAdapter;
import net.minecraft.SharedConstants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(value = ReplayModBackend.MODID)
public class ReplayModBackend {
   private ReplayMod mod;

   private ForgeEventAdapter eventsAdapter;

   public static final String MODID = "replaymod";

   public ReplayModBackend() {
      if (FMLEnvironment.dist.isClient()) {
         FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
         mod = new ReplayMod(this);
         eventsAdapter = new ForgeEventAdapter();
      }
   }

   public void init(FMLClientSetupEvent event) {
      mod.initModules();
      eventsAdapter.register();
      //hasIris = ModList.get().isLoaded("iris");
      // config = new Configuration(event.getSuggestedConfigurationFile());
      // config.load();
      // SettingsRegistry settingsRegistry = mod.getSettingsRegistry();
      // settingsRegistry.backend.setConfiguration(config);
      // settingsRegistry.save(); // Save default values to disk
   }

   public void onInitializeClient() {
      this.mod.initModules();
   }

   public String getVersion() {
      return "0";//todo FabricLoader.getInstance().getModContainer("replaymod").orElseThrow(IllegalStateException::new).getMetadata().getVersion().toString();
   }

   public String getMinecraftVersion() {
      return SharedConstants.getCurrentVersion().getName();
   }

   public boolean isModLoaded(String id) {
      return ModList.get().isLoaded(id.toLowerCase());
   }
}
