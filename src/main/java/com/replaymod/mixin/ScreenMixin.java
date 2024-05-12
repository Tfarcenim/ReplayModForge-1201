package com.replaymod.mixin;

import com.replaymod.compat.GuiScreenAccessor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin implements GuiScreenAccessor {
    @Shadow @Final public List<Renderable> renderables;

    @Shadow @Final private List<NarratableEntry> narratables;

    @Shadow @Final private List<GuiEventListener> children;

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T invokeAddButton(T var1) {
        this.renderables.add(var1);
        this.narratables.add(var1);
        this.children.add(var1);
        return var1;
    }
}
