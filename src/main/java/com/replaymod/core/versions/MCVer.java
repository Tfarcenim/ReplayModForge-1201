package com.replaymod.core.versions;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.replaymod.compat.GuiScreenAccessor;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector2f;
import com.replaymod.mixin.ParticleAccessor;
import com.replaymod.mixin.MainWindowAccessor;
import com.replaymod.replaystudio.lib.viaversion.api.protocol.packet.State;
import com.replaymod.replaystudio.lib.viaversion.api.protocol.version.ProtocolVersion;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.particle.Particle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MCVer {
   public static int getProtocolVersion() {
      return SharedConstants.getCurrentVersion().getProtocolVersion();
   }

   public static PacketTypeRegistry getPacketTypeRegistry(boolean loginPhase) {
      return PacketTypeRegistry.get(ProtocolVersion.getProtocol(getProtocolVersion()), loginPhase ? State.LOGIN : State.PLAY);
   }

   public static void resizeMainWindow(Minecraft mc, int width, int height) {
      Window window = mc.getWindow();
      MainWindowAccessor mainWindow = (MainWindowAccessor)(Object)window;
      mainWindow.invokeOnFramebufferSizeChanged(window.getWindow(), width, height);
   }

   public static CompletableFuture<?> setServerResourcePack(File file) {
      return getMinecraft().getDownloadedPackSource().setServerPack(file, PackSource.SERVER);
   }

   public static <T> void addCallback(CompletableFuture<T> future, Consumer<T> success, Consumer<Throwable> failure) {
      future.thenAccept(success).exceptionally((throwable) -> {
         failure.accept(throwable);
         return null;
      });
   }

   public static List<VertexFormatElement> getElements(VertexFormat vertexFormat) {
      return vertexFormat.getElements();
   }

   public static Minecraft getMinecraft() {
      return Minecraft.getInstance();
   }

   public static void addButton(Screen screen, Button button) {
      GuiScreenAccessor acc = (GuiScreenAccessor)screen;
      acc.invokeAddButton(button);
   }

   public static Optional<AbstractWidget> findButton(Iterable<? extends GuiEventListener> buttonList, String text, int id) {
      Component message = Component.translatable(text);

      for (GuiEventListener e : buttonList) {
         if (e instanceof ContainerEventHandler) {
            Optional<AbstractWidget> button = findButton(((ContainerEventHandler) e).children(), text, id);
            if (button.isPresent()) {
               return button;
            }
         }

         if (e instanceof AbstractWidget b) {
            if (message.equals(b.getMessage())) {
               return Optional.of(b);
            }

            if (b.getMessage() != null && b.getMessage().plainCopy().equals(message)) {
               return Optional.of(b);
            }
         }
      }

      return Optional.empty();
   }

   public static void processKeyBinds() {
      ((MCVer.MinecraftMethodAccessor)getMinecraft()).replayModProcessKeyBinds();
   }

   public static long milliTime() {
      return Util.getMillis();
   }

   public static Vec3 getPosition(Particle particle, float partialTicks) {
      ParticleAccessor acc = (ParticleAccessor)particle;
      double x = acc.getXo() + (acc.getPosX() - acc.getXo()) * (double)partialTicks;
      double y = acc.getYo() + (acc.getPosY() - acc.getYo()) * (double)partialTicks;
      double z = acc.getZo() + (acc.getPosZ() - acc.getZo()) * (double)partialTicks;
      return new Vec3(x, y, z);
   }

   public static void openFile(File file) {
      Util.getPlatform().openFile(file);
   }

   public static void openURL(URI url) {
      Util.getPlatform().openUri(url);
   }

   public static void pushMatrix() {
      RenderSystem.getModelViewStack().pushPose();
   }

   public static void popMatrix() {
      RenderSystem.getModelViewStack().popPose();
      RenderSystem.applyModelViewMatrix();
   }

   public static Quaternionf quaternion(float angle, Vector3f axis) {
      return (new Quaternionf()).fromAxisAngleDeg(axis.x, axis.y, axis.z, angle);
   }

   public static Matrix4f ortho(float left, float right, float top, float bottom, float zNear, float zFar) {
      return (new Matrix4f()).ortho(left, right, bottom, top, zNear, zFar);
   }

   public static void emitLine(BufferBuilder buffer, Vector2f p1, Vector2f p2, int color) {
      emitLine(buffer, new com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f(p1.x, p1.y, 0.0F), new com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f(p2.x, p2.y, 0.0F), color);
   }

   public static void emitLine(BufferBuilder buffer, com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f p1, com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f p2, int color) {
      int r = color >> 24 & 255;
      int g = color >> 16 & 255;
      int b = color >> 8 & 255;
      int a = color & 255;
      com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f n = com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.vector.Vector3f.sub(p2, p1, null);
      buffer.vertex(p1.x, p1.y, p1.z).color(r, g, b, a).normal(n.x, n.y, n.z).endVertex();
      buffer.vertex(p2.x, p2.y, p2.z).color(r, g, b, a).normal(n.x, n.y, n.z).endVertex();
   }

   public static void bindTexture(ResourceLocation id) {
      com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer.bindTexture(id);
   }

   public interface MinecraftMethodAccessor {
      void replayModProcessKeyBinds();

      void replayModExecuteTaskQueue();
   }

   public abstract static class Keyboard {
      public static final int KEY_LCONTROL = 341;
      public static final int KEY_LSHIFT = 340;
      public static final int KEY_ESCAPE = 256;
      public static final int KEY_HOME = 268;
      public static final int KEY_END = 269;
      public static final int KEY_UP = 265;
      public static final int KEY_DOWN = 264;
      public static final int KEY_LEFT = 263;
      public static final int KEY_RIGHT = 262;
      public static final int KEY_BACK = 259;
      public static final int KEY_DELETE = 261;
      public static final int KEY_RETURN = 257;
      public static final int KEY_TAB = 258;
      public static final int KEY_F1 = 290;
      public static final int KEY_A = 65;
      public static final int KEY_B = 66;
      public static final int KEY_C = 67;
      public static final int KEY_D = 68;
      public static final int KEY_E = 69;
      public static final int KEY_F = 70;
      public static final int KEY_G = 71;
      public static final int KEY_H = 72;
      public static final int KEY_I = 73;
      public static final int KEY_J = 74;
      public static final int KEY_K = 75;
      public static final int KEY_L = 76;
      public static final int KEY_M = 77;
      public static final int KEY_N = 78;
      public static final int KEY_O = 79;
      public static final int KEY_P = 80;
      public static final int KEY_Q = 81;
      public static final int KEY_R = 82;
      public static final int KEY_S = 83;
      public static final int KEY_T = 84;
      public static final int KEY_U = 85;
      public static final int KEY_V = 86;
      public static final int KEY_W = 87;
      public static final int KEY_X = 88;
      public static final int KEY_Y = 89;
      public static final int KEY_Z = 90;

      public static boolean hasControlDown() {
         return Screen.hasControlDown();
      }

      public static boolean isKeyDown(int keyCode) {
         return InputConstants.isKeyDown(MCVer.getMinecraft().getWindow().getWindow(), keyCode);
      }
   }
}
