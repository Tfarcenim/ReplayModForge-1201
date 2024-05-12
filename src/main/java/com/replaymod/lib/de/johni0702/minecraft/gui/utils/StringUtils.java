package com.replaymod.lib.de.johni0702.minecraft.gui.utils;

import com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;

public class StringUtils {
   public static String[] splitStringInMultipleRows(String string, int maxWidth) {
      if (string == null) {
         return new String[0];
      } else {
         Font fontRenderer = MCVer.getFontRenderer();
         List<String> rows = new ArrayList();
         String remaining = string;

         while(remaining.length() > 0) {
            String[] split = remaining.split(" ");
            String b = "";
            String[] var7 = split;
            int var8 = split.length;

            for(int var9 = 0; var9 < var8; ++var9) {
               String sp = var7[var9];
               b = b + sp + " ";
               if (fontRenderer.width(b.trim()) > maxWidth) {
                  b = b.substring(0, b.trim().length() - sp.length());
                  break;
               }
            }

            String trimmed = b.trim();
            rows.add(trimmed);

            try {
               remaining = remaining.substring(trimmed.length() + 1);
            } catch (Exception var11) {
               break;
            }
         }

         return (String[])rows.toArray(new String[rows.size()]);
      }
   }
}
