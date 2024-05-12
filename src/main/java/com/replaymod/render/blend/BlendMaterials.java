package com.replaymod.render.blend;

import com.mojang.blaze3d.platform.MemoryTracker;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.Image;
import com.replaymod.render.blend.data.DImage;
import com.replaymod.render.blend.data.DMaterial;
import com.replaymod.render.blend.data.DPackedFile;
import com.replaymod.render.blend.data.DTexture;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class BlendMaterials {
   private final Map<Integer, DMaterial> materials = new HashMap();

   public DMaterial getActiveMaterial() {
      int textureId = GL11.glGetInteger(32873);
      DMaterial material = this.materials.get(textureId);
      if (material == null) {
         int width = GL11.glGetTexLevelParameteri(3553, 0, 4096);
         int height = GL11.glGetTexLevelParameteri(3553, 0, 4097);
         ByteBuffer buffer = MemoryTracker.create(width * height * 4);
         GL11.glGetTexImage(3553, 0, 6408, 5121, buffer);
         Image bufImage = new Image(width, height);

         for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
               int a = buffer.get();
               int b = buffer.get();
               int g = buffer.get();
               int r = buffer.get();
               bufImage.setRGBA(x, y, r, b, g, a);
            }
         }

         ByteArrayOutputStream stream = new ByteArrayOutputStream();

         try {
            bufImage.writePNG(stream);
         } catch (IOException var13) {
            throw new RuntimeException(var13);
         }

         byte[] bytes = stream.toByteArray();
         DImage image = new DImage();
         image.id.name = "texture.png";
         image.filePath = "texture.png";
         image.packedFiles.add(Pair.of("texture.png", new DPackedFile(bytes)));
         DTexture texture = new DTexture();
         texture.image = image;
         DMaterial.DMTex mTex = new DMaterial.DMTex();
         mTex.texture = texture;
         material = new DMaterial();
         material.textures.add(mTex);
         this.materials.put(textureId, material);
      }

      return material;
   }
}
