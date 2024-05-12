package com.replaymod.core.versions;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.replaymod.mixin.MinecraftAccessor;
import com.replaymod.mixin.SimpleOptionAccessor;
import java.io.IOException;
import java.util.List;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

class Patterns {
   private static void addCrashCallable(CrashReportCategory category, String name, CrashReportDetail<String> callable) {
      category.setDetail(name, callable);
   }

   private static double Entity_getX(Entity entity) {
      return entity.getX();
   }

   private static double Entity_getY(Entity entity) {
      return entity.getY();
   }

   private static double Entity_getZ(Entity entity) {
      return entity.getZ();
   }

   private static void Entity_setYaw(Entity entity, float value) {
      entity.setYRot(value);
   }

   private static float Entity_getYaw(Entity entity) {
      return entity.getYRot();
   }

   private static void Entity_setPitch(Entity entity, float value) {
      entity.setXRot(value);
   }

   private static float Entity_getPitch(Entity entity) {
      return entity.getXRot();
   }

   private static void Entity_setPos(Entity entity, double x, double y, double z) {
      entity.setPosRaw(x, y, z);
   }

   private static int getX(AbstractWidget button) {
      return button.getX();
   }

   private static int getY(AbstractWidget button) {
      return button.getY();
   }

   private static void setX(AbstractWidget button, int value) {
      button.setX(value);
   }

   private static void setY(AbstractWidget button, int value) {
      button.setY(value);
   }

   private static void setWidth(AbstractWidget button, int value) {
      button.setWidth(value);
   }

   private static int getWidth(AbstractWidget button) {
      return button.getWidth();
   }

   private static int getHeight(AbstractWidget button) {
      return button.getHeight();
   }

   private static String readString(FriendlyByteBuf buffer, int max) {
      return buffer.readUtf(max);
   }

   private static Entity getRenderViewEntity(Minecraft mc) {
      return mc.getCameraEntity();
   }

   private static void setRenderViewEntity(Minecraft mc, Entity entity) {
      mc.setCameraEntity(entity);
   }

   private static Entity getVehicle(Entity passenger) {
      return passenger.getVehicle();
   }

   private static Inventory getInventory(Player entity) {
      return entity.getInventory();
   }

   private static Iterable<Entity> loadedEntityList(ClientLevel world) {
      return world.entitiesForRendering();
   }

   private static void getEntitySectionArray() {
   }

   private static List<? extends Player> playerEntities(Level world) {
      return world.players();
   }

   private static boolean isOnMainThread(Minecraft mc) {
      return mc.isSameThread();
   }

   private static void scheduleOnMainThread(Minecraft mc, Runnable runnable) {
      mc.tell(runnable);
   }

   private static Window getWindow(Minecraft mc) {
      return mc.getWindow();
   }

   private static BufferBuilder Tessellator_getBuffer(Tesselator tessellator) {
      return tessellator.getBuilder();
   }

   private static void BufferBuilder_beginPosCol() {
   }

   private static void BufferBuilder_addPosCol() {
   }

   private static void BufferBuilder_beginPosTex() {
   }

   private static void BufferBuilder_addPosTex() {
   }

   private static void BufferBuilder_beginPosTexCol() {
   }

   private static void BufferBuilder_addPosTexCol() {
   }

   private static Tesselator Tessellator_getInstance() {
      return Tesselator.getInstance();
   }

   private static EntityRenderDispatcher getEntityRenderDispatcher(Minecraft mc) {
      return mc.getEntityRenderDispatcher();
   }

   private static float getCameraYaw(EntityRenderDispatcher dispatcher) {
      return dispatcher.camera.getYRot();
   }

   private static float getCameraPitch(EntityRenderDispatcher dispatcher) {
      return dispatcher.camera.getXRot();
   }

   private static float getRenderPartialTicks(Minecraft mc) {
      return mc.getFrameTime();
   }

   private static TextureManager getTextureManager(Minecraft mc) {
      return mc.getTextureManager();
   }

   private static String getBoundKeyName(KeyMapping keyBinding) {
      return keyBinding.getTranslatedKeyMessage().getString();
   }

   private static SimpleSoundInstance master(ResourceLocation sound, float pitch) {
      return SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(sound), pitch);
   }

   private static boolean isKeyBindingConflicting(KeyMapping a, KeyMapping b) {
      return a.same(b);
   }

   private static void BufferBuilder_beginLineStrip(BufferBuilder buffer, VertexFormat vertexFormat) {
      buffer.begin(Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR_NORMAL);
   }

   private static void BufferBuilder_beginLines(BufferBuilder buffer) {
      buffer.begin(Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
   }

   private static void BufferBuilder_beginQuads(BufferBuilder buffer, VertexFormat vertexFormat) {
      buffer.begin(Mode.QUADS, vertexFormat);
   }

   private static void GL11_glLineWidth(float width) {
      RenderSystem.lineWidth(width);
   }

   private static void GL11_glTranslatef(float x, float y, float z) {
      RenderSystem.getModelViewStack().translate(x, y, z);
   }

   private static void GL11_glRotatef(float angle, float x, float y, float z) {
      RenderSystem.getModelViewStack().mulPose(MCVer.quaternion(angle, new Vector3f(x, y, z)));
   }

   private static Matrix4f getPositionMatrix(Pose stack) {
      return stack.pose();
   }

   private static void Futures_addCallback(ListenableFuture future, FutureCallback callback) {
      Futures.addCallback(future, callback, Runnable::run);
   }

   private static void setCrashReport(Minecraft mc, CrashReport report) {
      mc.delayCrashRaw(report);
   }

   private static ReportedException crashReportToException(Minecraft mc) {
      return new ReportedException((CrashReport)((MinecraftAccessor)mc).getCrashReporter().get());
   }

   private static Vec3 getTrackedPosition(Entity entity) {
      return entity.getPositionCodec().decode(0L, 0L, 0L);
   }

   private static Component newTextLiteral(String str) {
      return Component.literal(str);
   }

   private static Component newTextTranslatable(String key, Object... args) {
      return Component.translatable(key, args);
   }

   private static Vec3 getTrackedPos(Entity entity) {
      return entity.getPositionCodec().decode(0L, 0L, 0L);
   }

   private static void setGamma(Options options, double value) {
      ((SimpleOptionAccessor)(Object)options.gamma()).setRawValue(value);
   }

   private static double getGamma(Options options) {
      return (Double)options.gamma().get();
   }

   private static int getViewDistance(Options options) {
      return (Integer)options.renderDistance().get();
   }

   private static double getFov(Options options) {
      return (double)(Integer)options.fov().get();
   }

   private static int getGuiScale(Options options) {
      return (Integer)options.guiScale().get();
   }

   private static Resource getResource(ResourceManager manager, ResourceLocation id) throws IOException {
      return manager.getResourceOrThrow(id);
   }

   private static List<ItemStack> DefaultedList_ofSize_ItemStack_Empty(int size) {
      return NonNullList.withSize(size, ItemStack.EMPTY);
   }

   private static void setSoundVolume(Options options, SoundSource category, float value) {
      options.getSoundSourceOptionInstance(category).set((double)value);
   }

   private static SoundEvent SoundEvent_of(ResourceLocation identifier) {
      return SoundEvent.createVariableRangeEvent(identifier);
   }

   private static Vector3f POSITIVE_X() {
      return new Vector3f(1.0F, 0.0F, 0.0F);
   }

   private static Vector3f POSITIVE_Y() {
      return new Vector3f(0.0F, 1.0F, 0.0F);
   }

   private static Vector3f POSITIVE_Z() {
      return new Vector3f(0.0F, 0.0F, 1.0F);
   }

   private static Quaternionf getDegreesQuaternion(Vector3f axis, float angle) {
      return (new Quaternionf()).fromAxisAngleDeg(axis, angle);
   }

   private static void Quaternion_mul(Quaternionf left, Quaternionf right) {
      left.mul(right);
   }

   private static float Quaternion_getX(Quaternionf q) {
      return q.x;
   }

   private static float Quaternion_getY(Quaternionf q) {
      return q.y;
   }

   private static float Quaternion_getZ(Quaternionf q) {
      return q.z;
   }

   private static float Quaternion_getW(Quaternionf q) {
      return q.w;
   }

   private static Quaternionf Quaternion_copy(Quaternionf source) {
      return new Quaternionf(source);
   }

   private static void Matrix4f_multiply(Matrix4f left, Matrix4f right) {
      left.mul(right);
   }

   private static Matrix4f Matrix4f_translate(float x, float y, float z) {
      return (new Matrix4f()).translation(x, y, z);
   }

   private static Matrix4f Matrix4f_perspectiveMatrix(float left, float right, float top, float bottom, float zNear, float zFar) {
      return MCVer.ortho(left, right, top, bottom, zNear, zFar);
   }

   private static Registry<? extends Registry<?>> REGISTRIES() {
      return BuiltInRegistries.REGISTRY;
   }

   public Level getWorld(Entity entity) {
      return entity.level();
   }
}
