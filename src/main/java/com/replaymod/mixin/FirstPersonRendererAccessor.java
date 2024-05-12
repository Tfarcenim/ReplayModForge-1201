package com.replaymod.mixin;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ItemInHandRenderer.class})
public interface FirstPersonRendererAccessor {
   @Accessor("mainHandItem")
   void setItemStackMainHand(ItemStack var1);

   @Accessor("offHandItem")
   void setItemStackOffHand(ItemStack var1);

   @Accessor("mainHandHeight")
   void setEquippedProgressMainHand(float var1);

   @Accessor("oMainHandHeight")
   void setPrevEquippedProgressMainHand(float var1);

   @Accessor("offHandHeight")
   void setEquippedProgressOffHand(float var1);

   @Accessor("oOffHandHeight")
   void setPrevEquippedProgressOffHand(float var1);
}
