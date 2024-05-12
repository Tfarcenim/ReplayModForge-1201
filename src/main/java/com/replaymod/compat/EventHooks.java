package com.replaymod.compat;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EventHooks {

    @SubscribeEvent
    public static void skipHUDDuringRender(RenderGuiEvent.Pre event) {
        if (MixinHooks.shouldHideHUD()) {
            event.setCanceled(true);
        }
    }

}
