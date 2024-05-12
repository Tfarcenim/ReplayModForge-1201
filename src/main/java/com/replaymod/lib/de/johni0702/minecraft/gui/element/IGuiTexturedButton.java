package com.replaymod.lib.de.johni0702.minecraft.gui.element;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public interface IGuiTexturedButton<T extends IGuiTexturedButton<T>> extends IGuiClickable<T> {
   ResourceLocation getTexture();

   ReadableDimension getTextureTotalSize();

   T setTexture(ResourceLocation var1, int var2);

   T setTexture(ResourceLocation var1, int var2, int var3);

   ReadableDimension getTextureSize();

   T setTextureSize(int var1);

   T setTextureSize(int var1, int var2);

   ReadablePoint getTextureNormal();

   ReadablePoint getTextureHover();

   ReadablePoint getTextureDisabled();

   T setTexturePosH(int var1, int var2);

   T setTexturePosV(int var1, int var2);

   T setTexturePosH(ReadablePoint var1);

   T setTexturePosV(ReadablePoint var1);

   T setTexturePos(int var1, int var2, int var3, int var4);

   T setTexturePos(ReadablePoint var1, ReadablePoint var2);

   T setTexturePos(int var1, int var2, int var3, int var4, int var5, int var6);

   T setTexturePos(ReadablePoint var1, ReadablePoint var2, ReadablePoint var3);

   T setSound(SoundEvent var1);
}
