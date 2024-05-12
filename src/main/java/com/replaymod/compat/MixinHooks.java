package com.replaymod.compat;

import com.replaymod.core.versions.MCVer;
import com.replaymod.render.hooks.EntityRendererHandler;

public class MixinHooks {

    public static EntityRendererHandler getHandler() {
        return ((EntityRendererHandler.IEntityRenderer) MCVer.getMinecraft().gameRenderer).replayModRender_getHandler();
    }

    public static boolean shouldHideHUD() {
        EntityRendererHandler entityRendererHandler = getHandler();
        return entityRendererHandler != null;
    }
}
