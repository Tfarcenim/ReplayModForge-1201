package com.replaymod.mixin;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ClientHandshakePacketListenerImpl.class})
public interface ClientLoginNetworkHandlerAccessor {
   @Accessor
   ServerData getServerData();
}
