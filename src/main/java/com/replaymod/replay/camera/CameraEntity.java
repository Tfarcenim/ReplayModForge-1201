package com.replaymod.replay.camera;

import com.replaymod.core.KeyBindingRegistry;
import com.replaymod.core.ReplayMod;
import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.events.KeyBindingEventCallback;
import com.replaymod.core.events.PreRenderCallback;
import com.replaymod.core.events.PreRenderHandCallback;
import com.replaymod.core.events.SettingsChangedCallback;
import com.replaymod.core.utils.Utils;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.PreTickCallback;
import com.replaymod.mixin.EntityPlayerAccessor;
import com.replaymod.mixin.FirstPersonRendererAccessor;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.Setting;
import com.replaymod.replay.events.RenderHotbarCallback;
import com.replaymod.replay.events.RenderSpectatorCrosshairCallback;
import com.replaymod.replaystudio.util.Location;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class CameraEntity extends LocalPlayer {
   private static final UUID CAMERA_UUID;
   public float roll;
   private CameraController cameraController;
   private long lastControllerUpdate = System.currentTimeMillis();
   private Entity lastHandRendered = null;
   private CameraEntity.EventHandler eventHandler = new CameraEntity.EventHandler();
   private final Inventory originalInventory = this.getInventory();

   public CameraEntity(Minecraft mcIn, ClientLevel worldIn, ClientPacketListener netHandlerPlayClient, StatsCounter statisticsManager, ClientRecipeBook recipeBook) {
      super(mcIn, worldIn, netHandlerPlayClient, statisticsManager, recipeBook, false, false);
      this.setUUID(CAMERA_UUID);
      this.eventHandler.register();
      if (ReplayModReplay.instance.getReplayHandler().getSpectatedUUID() == null) {
         this.cameraController = ReplayModReplay.instance.createCameraController(this);
      } else {
         this.cameraController = new SpectatorCameraController(this);
      }

   }

   public CameraController getCameraController() {
      return this.cameraController;
   }

   public void setCameraController(CameraController cameraController) {
      this.cameraController = cameraController;
   }

   public void moveCamera(double x, double y, double z) {
      this.setCameraPosition(this.getX() + x, this.getY() + y, this.getZ() + z);
   }

   public void setCameraPosition(double x, double y, double z) {
      this.xOld = this.xo = x;
      this.yOld = this.yo = y;
      this.zOld = this.zo = z;
      this.setPosRaw(x, y, z);
      this.updateBoundingBox();
   }

   public void setCameraRotation(float yaw, float pitch, float roll) {
      this.yRotO = yaw;
      this.xRotO = pitch;
      this.setYRot(yaw);
      this.setXRot(pitch);
      this.roll = roll;
   }

   public void setCameraPosRot(Location pos) {
      this.setCameraRotation(pos.getYaw(), pos.getPitch(), this.roll);
      this.setCameraPosition(pos.getX(), pos.getY(), pos.getZ());
   }

   public void setCameraPosRot(Entity to) {
      if (to != this) {
         float yOffset = 0.0F;
         this.xo = to.xo;
         this.yo = to.yo + (double)yOffset;
         this.zo = to.zo;
         this.yRotO = to.yRotO;
         this.xRotO = to.xRotO;
         this.setPosRaw(to.getX(), to.getY(), to.getZ());
         this.setYRot(to.getYRot());
         this.setXRot(to.getXRot());
         this.xOld = to.xOld;
         this.yOld = to.yOld + (double)yOffset;
         this.zOld = to.zOld;
         this.wrapArmYaw();
         this.updateBoundingBox();
      }
   }

   public float getViewYRot(float tickDelta) {
      Entity view = this.minecraft.getCameraEntity();
      return view != null && view != this ? this.yRotO + (this.getYRot() - this.yRotO) * tickDelta : super.getViewYRot(tickDelta);
   }

   public float getViewXRot(float tickDelta) {
      Entity view = this.minecraft.getCameraEntity();
      return view != null && view != this ? this.xRotO + (this.getXRot() - this.xRotO) * tickDelta : super.getViewXRot(tickDelta);
   }

   private void updateBoundingBox() {
      float width = this.getBbWidth();
      float height = this.getBbHeight();
      this.setBoundingBox(new AABB(this.getX() - (double)(width / 2.0F), this.getY(), this.getZ() - (double)(width / 2.0F), this.getX() + (double)(width / 2.0F), this.getY() + (double)height, this.getZ() + (double)(width / 2.0F)));
   }

   public void tick() {
      Entity view = this.minecraft.getCameraEntity();
      if (view != null) {
         UUID spectating = ReplayModReplay.instance.getReplayHandler().getSpectatedUUID();
         if (spectating != null && (view.getUUID() != spectating || view.level() != this.level()) || this.level().getEntity(view.getId()) != view) {
            if (spectating == null) {
               ReplayModReplay.instance.getReplayHandler().spectateEntity(this);
               return;
            }

            view = this.level().getPlayerByUUID(spectating);
            if (view == null) {
               this.minecraft.setCameraEntity(this);
               return;
            }

            this.minecraft.setCameraEntity(view);
         }

         if (view != this) {
            this.setCameraPosRot(view);
         }
      }

   }

   public void resetPos() {
      if (this.minecraft.level != null) {
         this.setLevel(this.minecraft.level);
      }

      super.resetPos();
   }

   public void setRot(float yaw, float pitch) {
      if (this.minecraft.getCameraEntity() == this) {
         super.setRot(yaw, pitch);
      }

   }

   public boolean isInWall() {
      return this.falseUnlessSpectating(Entity::isInWall);
   }

   public boolean isEyeInFluid(TagKey<Fluid> fluid) {
      return this.falseUnlessSpectating((entity) -> {
         return entity.isEyeInFluid(fluid);
      });
   }

   public float getWaterVision() {
      return this.falseUnlessSpectating((__) -> {
         return true;
      }) ? super.getWaterVision() : 1.0F;
   }

   public boolean isOnFire() {
      return this.falseUnlessSpectating(Entity::isOnFire);
   }

   private boolean falseUnlessSpectating(Function<Entity, Boolean> property) {
      Entity view = this.minecraft.getCameraEntity();
      return view != null && view != this ? property.apply(view) : false;
   }

   public boolean isPushable() {
      return false;
   }

   protected void spawnSprintParticle() {
   }

   public boolean isPickable() {
      return false;
   }

   public boolean isSpectator() {
      ReplayHandler replayHandler = ReplayModReplay.instance.getReplayHandler();
      return replayHandler == null || replayHandler.isCameraView();
   }

   public boolean shouldRender(double double_1, double double_2, double double_3) {
      return false;
   }

   public float getFieldOfViewModifier() {
      Entity view = this.minecraft.getCameraEntity();
      return view != this && view instanceof AbstractClientPlayer ? ((AbstractClientPlayer)view).getFieldOfViewModifier() : 1.0F;
   }

   public boolean isInvisible() {
      Entity view = this.minecraft.getCameraEntity();
      return view != this ? view.isInvisible() : super.isInvisible();
   }

   public ResourceLocation getSkinTextureLocation() {
      Entity view = this.minecraft.getCameraEntity();
      return view != this && view instanceof AbstractClientPlayer ? ((AbstractClientPlayer)view).getSkinTextureLocation() : super.getSkinTextureLocation();
   }

   public String getModelName() {
      Entity view = this.minecraft.getCameraEntity();
      return view != this && view instanceof AbstractClientPlayer ? ((AbstractClientPlayer)view).getModelName() : super.getModelName();
   }

   public boolean isModelPartShown(PlayerModelPart modelPart) {
      Entity view = this.minecraft.getCameraEntity();
      return view != this && view instanceof Player ? ((Player)view).isModelPartShown(modelPart) : super.isModelPartShown(modelPart);
   }

   public HumanoidArm getMainArm() {
      Entity view = this.minecraft.getCameraEntity();
      return view != this && view instanceof Player ? ((Player)view).getMainArm() : super.getMainArm();
   }

   public float getAttackAnim(float renderPartialTicks) {
      Entity view = this.minecraft.getCameraEntity();
      return view != this && view instanceof Player ? ((Player)view).getAttackAnim(renderPartialTicks) : 0.0F;
   }

   public float getCurrentItemAttackStrengthDelay() {
      Entity view = this.minecraft.getCameraEntity();
      return view != this && view instanceof Player ? ((Player)view).getCurrentItemAttackStrengthDelay() : 1.0F;
   }

   public float getAttackStrengthScale(float adjustTicks) {
      Entity view = this.minecraft.getCameraEntity();
      return view != this && view instanceof Player ? ((Player)view).getAttackStrengthScale(adjustTicks) : 1.0F;
   }

   public InteractionHand getUsedItemHand() {
      Entity view = this.minecraft.getCameraEntity();
      return view != this && view instanceof Player ? ((Player)view).getUsedItemHand() : super.getUsedItemHand();
   }

   public boolean isUsingItem() {
      Entity view = this.minecraft.getCameraEntity();
      return view != this && view instanceof Player ? ((Player)view).isUsingItem() : super.isUsingItem();
   }

   public void onEquipItem(EquipmentSlot slot, ItemStack stack, ItemStack itemStack) {
   }

   public HitResult pick(double maxDistance, float tickDelta, boolean fluids) {
      HitResult result = super.pick(maxDistance, tickDelta, fluids);
      if (result instanceof BlockHitResult blockResult) {
         result = BlockHitResult.miss(result.getLocation(), blockResult.getDirection(), blockResult.getBlockPos());
      }

      return result;
   }

   public void remove(RemovalReason reason) {
      super.remove(reason);
      if (this.eventHandler != null) {
         this.eventHandler.unregister();
         this.eventHandler = null;
      }

   }

   private void update() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != this.level()) {
         if (this.eventHandler != null) {
            this.eventHandler.unregister();
            this.eventHandler = null;
         }

      } else {
         long now = System.currentTimeMillis();
         long timePassed = now - this.lastControllerUpdate;
         this.cameraController.update((float)timePassed / 50.0F);
         this.lastControllerUpdate = now;
         this.handleInputEvents();
         Map<String, KeyBindingRegistry.Binding> keyBindings = ReplayMod.instance.getKeyBindingRegistry().getBindings();
         if (keyBindings.get("key.replaymod.rollclockwise").keyBinding.isDown()) {
            this.roll = (float)((double)this.roll + (Utils.isCtrlDown() ? 0.2D : 1.0D));
         }

         if (keyBindings.get("key.replaymod.rollcounterclockwise").keyBinding.isDown()) {
            this.roll = (float)((double)this.roll - (Utils.isCtrlDown() ? 0.2D : 1.0D));
         }

         this.noPhysics = this.isSpectator();
         this.syncInventory();
      }
   }

   private void syncInventory() {
      Entity view = this.minecraft.getCameraEntity();
      Player viewPlayer = view != this && view instanceof Player ? (Player)view : null;
      EntityPlayerAccessor cameraA = (EntityPlayerAccessor)this;
      EntityPlayerAccessor viewPlayerA = (EntityPlayerAccessor)viewPlayer;
      ItemStack empty = ItemStack.EMPTY;
      this.setItemSlot(EquipmentSlot.HEAD, viewPlayer != null ? viewPlayer.getItemBySlot(EquipmentSlot.HEAD) : empty);
      this.setItemSlot(EquipmentSlot.MAINHAND, viewPlayer != null ? viewPlayer.getItemBySlot(EquipmentSlot.MAINHAND) : empty);
      this.setItemSlot(EquipmentSlot.OFFHAND, viewPlayer != null ? viewPlayer.getItemBySlot(EquipmentSlot.OFFHAND) : empty);
      cameraA.setItemStackMainHand(viewPlayerA != null ? viewPlayerA.getItemStackMainHand() : empty);
      this.swingingArm = viewPlayer != null ? viewPlayer.swingingArm : InteractionHand.MAIN_HAND;
      this.useItem = viewPlayer != null ? viewPlayer.getUseItem() : empty;
      cameraA.setActiveItemStackUseCount(viewPlayerA != null ? viewPlayerA.getActiveItemStackUseCount() : 0);
   }

   private void handleInputEvents() {
      if (this.minecraft.options.keyAttack.consumeClick() || this.minecraft.options.keyUse.consumeClick()) {
         if (this.minecraft.screen == null && canSpectate(this.minecraft.crosshairPickEntity)) {
            ReplayModReplay.instance.getReplayHandler().spectateEntity(this.minecraft.crosshairPickEntity);
            // Make sure we don't exit right away
            // noinspection StatementWithEmptyBody
            while (this.minecraft.options.keyShift.consumeClick())
               ;
         }
      }
   }

   private void updateArmYawAndPitch() {
      this.yBobO = this.yBob;
      this.xBobO = this.xBob;
      this.xBob += (this.getXRot() - this.xBob) * 0.5F;
      this.yBob += wrapDegrees(this.getYRot() - this.yBob) * 0.5F;
      this.wrapArmYaw();
   }

   private void wrapArmYaw() {
      this.yBob = wrapDegreesTo(this.yBob, this.getYRot());
      this.yBobO = wrapDegreesTo(this.yBobO, this.yBob);
   }

   private static float wrapDegreesTo(float value, float towardsValue) {
      while(towardsValue - value < -180.0F) {
         value -= 360.0F;
      }

      while(towardsValue - value >= 180.0F) {
         value += 360.0F;
      }

      return value;
   }

   private static float wrapDegrees(float value) {
      value %= 360.0F;
      return wrapDegreesTo(value, 0.0F);
   }

   public boolean canSpectate(Entity e) {
      return e != null && !e.isInvisible();
   }

   static {
      CAMERA_UUID = UUID.nameUUIDFromBytes("ReplayModCamera".getBytes(StandardCharsets.UTF_8));
   }

   private class EventHandler extends EventRegistrations {
      private final Minecraft mc = MCVer.getMinecraft();
      private boolean heldItemTooltipsWasTrue;

      private EventHandler() {
         this.on(PreTickCallback.EVENT, this::onPreClientTick);
         this.on(PreRenderCallback.EVENT, this::onRenderUpdate);
         this.on(KeyBindingEventCallback.EVENT, CameraEntity.this::handleInputEvents);
         this.on(RenderSpectatorCrosshairCallback.EVENT, this::shouldRenderSpectatorCrosshair);
         this.on(RenderHotbarCallback.EVENT, this::shouldRenderHotbar);
         this.on(SettingsChangedCallback.EVENT, this::onSettingsChanged);
         this.on(PreRenderHandCallback.EVENT, this::onRenderHand);
      }

      private void onPreClientTick() {
         CameraEntity.this.updateArmYawAndPitch();
      }

      private void onRenderUpdate() {
         CameraEntity.this.update();
      }

      private Boolean shouldRenderSpectatorCrosshair() {
         return CameraEntity.this.canSpectate(this.mc.crosshairPickEntity);
      }

      private Boolean shouldRenderHotbar() {
         return false;
      }

      private void onSettingsChanged(SettingsRegistry registry, SettingsRegistry.SettingKey<?> key) {
         if (key == Setting.CAMERA) {
            if (ReplayModReplay.instance.getReplayHandler().getSpectatedUUID() == null) {
               CameraEntity.this.cameraController = ReplayModReplay.instance.createCameraController(CameraEntity.this);
            } else {
               CameraEntity.this.cameraController = new SpectatorCameraController(CameraEntity.this);
            }
         }

      }

      private boolean onRenderHand() {
         Entity view = this.mc.getCameraEntity();
         if (view != CameraEntity.this && view instanceof Player player) {
            if (CameraEntity.this.lastHandRendered != player) {
               CameraEntity.this.lastHandRendered = player;
               FirstPersonRendererAccessor acc = (FirstPersonRendererAccessor)this.mc.gameRenderer.itemInHandRenderer;
               acc.setPrevEquippedProgressMainHand(1.0F);
               acc.setPrevEquippedProgressOffHand(1.0F);
               acc.setEquippedProgressMainHand(1.0F);
               acc.setEquippedProgressOffHand(1.0F);
               acc.setItemStackMainHand(player.getItemBySlot(EquipmentSlot.MAINHAND));
               acc.setItemStackOffHand(player.getItemBySlot(EquipmentSlot.OFFHAND));
               this.mc.player.yBob = this.mc.player.yBobO = player.getYRot();
               this.mc.player.xBob = this.mc.player.xBobO = player.getXRot();
            }

            return false;
         } else {
            return true;
         }
      }
   }
}
