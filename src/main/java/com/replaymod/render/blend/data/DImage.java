package com.replaymod.render.blend.data;


import org.apache.commons.lang3.tuple.Pair;
import org.blender.dna.Image;
import org.blender.dna.ImagePackedFile;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.nio.CPointer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DImage {
   public final DId id;
   public String filePath;
   public List<Pair<String, DPackedFile>> packedFiles;

   public DImage() {
      this.id = new DId(BlockCodes.ID_IM);
      this.packedFiles = new ArrayList<>();
   }

   public CPointer<Image> serialize(Serializer serializer) throws IOException {
      return serializer.maybeMajor(this, this.id, Image.class, () -> {
         return (image) -> {
            image.getName().fromString(String.valueOf(this.filePath));
            image.setSource((short)1);
            image.getColorspace_settings().getName().fromString("sRGB");
            image.setAspx(1.0F);
            image.setAspy(1.0F);
            serializer.writeDataList(ImagePackedFile.class, image.getPackedfiles(), this.packedFiles.size(), (i, pf) -> {
               Pair<String, DPackedFile> pair = this.packedFiles.get(i);
               pf.getFilepath().fromString((String)pair.getLeft());
               pf.setPackedfile(pair.getRight().serialize(serializer));
            });
         };
      });
   }
}
