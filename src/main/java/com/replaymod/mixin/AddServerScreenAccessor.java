package com.replaymod.mixin;

import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({EditServerScreen.class})
public interface AddServerScreenAccessor {
   @Accessor
   ServerData getServerData();
}
