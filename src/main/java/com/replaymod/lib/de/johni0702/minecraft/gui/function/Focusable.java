package com.replaymod.lib.de.johni0702.minecraft.gui.function;

import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Consumer;

public interface Focusable<T extends Focusable<T>> {
   boolean isFocused();

   T setFocused(boolean var1);

   T onFocusChange(Consumer<Boolean> var1);

   Focusable getNext();

   T setNext(Focusable var1);

   Focusable getPrevious();

   T setPrevious(Focusable var1);
}
