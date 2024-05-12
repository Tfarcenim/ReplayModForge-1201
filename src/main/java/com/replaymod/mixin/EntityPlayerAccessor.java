package com.replaymod.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Player.class})
public interface EntityPlayerAccessor extends EntityLivingBaseAccessor {
   @Accessor("lastItemInMainHand")
   ItemStack getItemStackMainHand();

   @Accessor("lastItemInMainHand")
   void setItemStackMainHand(ItemStack var1);
}
