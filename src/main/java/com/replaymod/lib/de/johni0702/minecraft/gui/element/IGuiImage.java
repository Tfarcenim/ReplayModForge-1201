package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;
import net.minecraft.resources.ResourceLocation;

public interface IGuiImage<T extends IGuiImage<T>> extends GuiElement<T> {
   T setTexture(Image var1);

   T setTexture(ResourceLocation var1);

   T setTexture(ResourceLocation var1, int var2, int var3, int var4, int var5);

   T setU(int var1);

   T setV(int var1);

   T setUV(int var1, int var2);

   T setUWidth(int var1);

   T setVHeight(int var1);

   T setUVSize(int var1, int var2);
}
