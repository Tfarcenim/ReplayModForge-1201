package com.replaymod.mixin;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({OptionInstance.class})
public interface SimpleOptionAccessor<T> {
   @Accessor("value")
   void setRawValue(T var1);
}
