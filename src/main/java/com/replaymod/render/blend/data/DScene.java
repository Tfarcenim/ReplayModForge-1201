package com.replaymod.render.blend.data;


import org.blender.dna.Base;
import org.blender.dna.BlenderObject;
import org.blender.dna.RenderData;
import org.blender.dna.Scene;
import org.blender.dna.ToolSettings;
import org.cakelab.blender.io.block.BlockCodes;
import org.cakelab.blender.nio.CPointer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DScene {
   public final DId id;
   public final List<DObject> base;
   public int endFrame;

   public DScene() {
      this.id = new DId(BlockCodes.ID_SCE, "Scene");
      this.base = new ArrayList<>();
   }

   private Set<DObject> findAllObjects(DObject curr, Set<DObject> set) {

      for (DObject child : curr == null ? this.base : curr.getChildren()) {
         if (child.isValid()) {
            this.findAllObjects(child, set);
            set.add(child);
         }
      }

      return set;
   }

   public CPointer<Scene> serialize(Serializer serializer) throws IOException {
      return serializer.maybeMajor(this, this.id, Scene.class, () -> {
         Set<DObject> allObjects = this.findAllObjects(null, new LinkedHashSet<>());
         List<CPointer<BlenderObject>> bases = new ArrayList<>(allObjects.size());

         for (DObject dObject : allObjects) {
            bases.add(dObject.serialize(serializer));
         }

         return (scene) -> {
            serializer.writeDataList(Base.class, scene.getBase(), bases.size(), (i, base) -> {
               CPointer<BlenderObject> objPointer = bases.get(i);
               base.setLay(objPointer.get().getLay());
               base.setObject(objPointer);
            });
            RenderData renderData = scene.getR();
            renderData.setXsch(1920);
            renderData.setYsch(1080);
            renderData.setSize((short) 100);
            renderData.setXasp(1.0F);
            renderData.setYasp(1.0F);
            renderData.setTilex(64);
            renderData.setTiley(64);
            renderData.setEfra(this.endFrame);
            renderData.setFrame_step(1);
            renderData.setFramapto(100);
            renderData.setImages(100);
            ToolSettings toolSettings = serializer.writeData(ToolSettings.class);
            scene.setToolsettings(toolSettings.__io__addressof());
         };
      });
   }
}
