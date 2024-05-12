package com.replaymod.replay.handler;

import com.replaymod.core.gui.GuiReplayButton;
import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.MinecraftGuiRenderer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTooltip;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Point;
import com.replaymod.lib.de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.Setting;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GuiHandler extends EventRegistrations {
   private static final int BUTTON_REPLAY_VIEWER = 17890234;
   private static final int BUTTON_EXIT_REPLAY = 17890235;
   private final ReplayModReplay mod;

   public GuiHandler(ReplayModReplay mod) {
      this.on(InitScreenCallback.EVENT, this::injectIntoIngameMenu);
      this.on(InitScreenCallback.EVENT, (screen, buttons) -> {
         this.ensureReplayStopped(screen);
      });
      this.on(InitScreenCallback.EVENT, this::injectIntoMainMenu);
      this.mod = mod;
   }

   private void injectIntoIngameMenu(Screen guiScreen, Collection<AbstractWidget> buttonList) {
      if (guiScreen instanceof PauseScreen) {
         if (this.mod.getReplayHandler() != null) {
            this.mod.getReplayHandler().getReplaySender().setReplaySpeed(0.0D);
            Component BUTTON_OPTIONS = Component.translatable("menu.options");
            Component BUTTON_EXIT_SERVER = Component.translatable("menu.disconnect");
            Component BUTTON_ADVANCEMENTS = Component.translatable("gui.advancements");
            Component BUTTON_STATS = Component.translatable("gui.stats");
            Component BUTTON_OPEN_TO_LAN = Component.translatable("menu.shareToLan");
            AbstractWidget achievements = null;
            AbstractWidget stats = null;

            for (AbstractWidget o : new ArrayList<>(buttonList)) {
               boolean remove = false;
               Component id = o.getMessage();
               if (id != null) {
                  if (id.equals(BUTTON_EXIT_SERVER)) {
                     remove = true;
                     MCVer.addButton(guiScreen, new InjectedButton(guiScreen, 17890235, o.getX(), o.getY(), o.getWidth(), o.getHeight(), "replaymod.gui.exit", null, this::onButton));
                  } else if (id.equals(BUTTON_ADVANCEMENTS)) {
                     remove = true;
                     achievements = o;
                  } else if (id.equals(BUTTON_STATS)) {
                     remove = true;
                     stats = o;
                  } else if (id.equals(BUTTON_OPEN_TO_LAN)) {
                     remove = true;
                  }

                  if (remove) {
                     o.setX(-1000);
                     o.setY(-1000);
                  }
               }
            }

            if (achievements != null && stats != null) {
               this.moveAllButtonsInRect(buttonList, achievements.getX(), stats.getX() + stats.getWidth(), achievements.getY(), Integer.MAX_VALUE, -24);
            }
         }

      }
   }

   private void moveAllButtonsInRect(Collection<AbstractWidget> buttons, int xStart, int xEnd, int yStart, int yEnd, int moveBy) {
      buttons.stream().filter((button) -> {
         return button.getX() <= xEnd && button.getX() + button.getWidth() >= xStart;
      }).filter((button) -> {
         return button.getY() <= yEnd && button.getY() + button.getHeight() >= yStart;
      }).forEach((button) -> {
         button.setY(button.getY() + moveBy);
      });
   }

   private void ensureReplayStopped(Screen guiScreen) {
      if (guiScreen instanceof TitleScreen || guiScreen instanceof JoinMultiplayerScreen) {
         if (this.mod.getReplayHandler() != null) {
            try {
               this.mod.getReplayHandler().endReplay();
            } catch (IOException var6) {
               ReplayModReplay.LOGGER.error("Trying to stop broken replay: ", var6);
            } finally {
               if (this.mod.getReplayHandler() != null) {
                  this.mod.forcefullyStopReplay();
               }

            }
         }

      }
   }

   private void injectIntoMainMenu(Screen guiScreen, Collection<AbstractWidget> buttonList) {
      if (guiScreen instanceof TitleScreen) {
        // if (this.mod.getCore().getSettingsRegistry().get(Setting.LEGACY_MAIN_MENU_BUTTON)) {
            this.legacyInjectIntoMainMenu(guiScreen, buttonList);
     //    } else {
     //       this.properInjectIntoMainMenu(guiScreen,buttonList);
     //    }

      }
   }

   /*private void properInjectIntoMainMenu(Screen screen,Collection<AbstractWidget> buttonList) {
      GuiHandler.MainMenuButtonPosition buttonPosition = GuiHandler.MainMenuButtonPosition.valueOf(this.mod.getCore().getSettingsRegistry().get(Setting.MAIN_MENU_BUTTON));

      Point pos;
      if (buttonPosition == GuiHandler.MainMenuButtonPosition.BIG) {
         int x = screen.width / 2 - 100;
         Optional<AbstractWidget> targetButton = MCVer.findButton(buttonList, "menu.online", 14).or(() -> MCVer.findButton(buttonList, "menu.multiplayer", 2));
         int y = targetButton.map(AbstractWidget::getY).orElse(screen.height / 4 + 10 + 96);
         this.moveAllButtonsInRect(buttonList, x, x + 200, Integer.MIN_VALUE, y, -24);
         pos = new Point(x, y);
      } else {
         pos = this.determineButtonPos(buttonPosition, screen, buttonList);
      }

      GuiHandler.InjectedButton replayViewerButton;
      if (buttonPosition == GuiHandler.MainMenuButtonPosition.BIG) {
         replayViewerButton = new GuiHandler.InjectedButton(screen, 17890234, pos.getX(), pos.getY(), 200, 20, "replaymod.gui.replayviewer", null, this::onButton);
      } else {
         replayViewerButton = new GuiHandler.InjectedButton(screen, 17890234, pos.getX(), pos.getY(), 20, 20, "", "replaymod.gui.replayviewer", this::onButton) {
            public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
               super.renderWidget(context, mouseX, mouseY, delta);
               MinecraftGuiRenderer renderer = new MinecraftGuiRenderer(context);
               renderer.bindTexture(GuiReplayButton.ICON);
               renderer.drawTexturedRect(this.getX() + 3, this.getY() + 3, 0, 0, this.width - 6, this.height - 6, 1, 1, 1, 1);
            }
         };
      }

      int index = this.determineButtonIndex(buttonList, replayViewerButton);
      if (index != -1) {
         buttonList.add(index, replayViewerButton);
      } else {
         buttonList.add(replayViewerButton);
      }

   }*/

   private void legacyInjectIntoMainMenu(Screen guiScreen, Collection<AbstractWidget> buttonList) {
      boolean isCustomMainMenuMod = guiScreen.getClass().getName().endsWith("custommainmenu.gui.GuiFakeMain");
      final GuiHandler.MainMenuButtonPosition buttonPosition = GuiHandler.MainMenuButtonPosition.valueOf(this.mod.getCore().getSettingsRegistry().get(Setting.MAIN_MENU_BUTTON));
      if (buttonPosition != GuiHandler.MainMenuButtonPosition.BIG && !isCustomMainMenuMod) {
         VanillaGuiScreen vanillaGui = VanillaGuiScreen.wrap(guiScreen);
         final GuiReplayButton replayButton = new GuiReplayButton();
         replayButton.onClick(() -> {
            new GuiReplayViewer(this.mod).display();
         }).setTooltip((new GuiTooltip()).setI18nText("replaymod.gui.replayviewer"));
         vanillaGui.setLayout(new CustomLayout<GuiScreen>(vanillaGui.getLayout()) {
            private Point pos;

            protected void layout(GuiScreen container, int width, int height) {
               if (this.pos == null) {
                  this.pos = GuiHandler.this.determineButtonPos(buttonPosition, guiScreen, buttonList);
               }

               this.size(replayButton, 20, 20);
               this.pos(replayButton, this.pos.getX(), this.pos.getY());
            }
         }).addElements(null, replayButton);
      } else {
         int x = guiScreen.width / 2 - 100;
         int y = MCVer.findButton(buttonList, "menu.online", 14).or(() -> MCVer.findButton(buttonList, "menu.multiplayer", 2))
                 .map(AbstractWidget::getY).orElse(guiScreen.height / 4 + 10 + 96);
         this.moveAllButtonsInRect(buttonList, x, x + 200, Integer.MIN_VALUE, y, -24);
         GuiHandler.InjectedButton button = new GuiHandler.InjectedButton(guiScreen, 17890234, x, y, 200, 20, "replaymod.gui.replayviewer", null, this::onButton);
         MCVer.addButton(guiScreen, button);
      }
   }

   private Point determineButtonPos(GuiHandler.MainMenuButtonPosition buttonPosition, Screen guiScreen, Collection<AbstractWidget> buttonList) {
      Point topRight = new Point(guiScreen.width - 20 - 5, 5);
      if (buttonPosition == GuiHandler.MainMenuButtonPosition.TOP_LEFT) {
         return new Point(5, 5);
      } else if (buttonPosition == GuiHandler.MainMenuButtonPosition.TOP_RIGHT) {
         return topRight;
      } else {
         return buttonPosition == GuiHandler.MainMenuButtonPosition.DEFAULT ? Stream.of(MCVer.findButton(buttonList, "menu.singleplayer", 1),
                 MCVer.findButton(buttonList, "menu.multiplayer", 2), MCVer.findButton(buttonList, "menu.online", 14),
                 MCVer.findButton(buttonList, "modmenu.title", 6)).flatMap(Optional::stream)
                 .filter((it) -> buttonList.stream().noneMatch((button) -> button.getX() <= it.getX() + it.getWidth() + 4 + 20 && button.getY() <= it.getY()
                         + it.getHeight() && button.getX() + button.getWidth() >= it.getX() + it.getWidth() + 4 && button.getY() + button.getHeight() >=
                         it.getY())).max(Comparator.comparingInt(AbstractWidget::getY).thenComparingInt(AbstractWidget::getX))
                 .map((it) -> new Point(it.getX() + it.getWidth() + 4, it.getY()))
                 .orElse(topRight) : Optional.of(buttonList).flatMap((buttons) -> {
            switch(buttonPosition) {
            case LEFT_OF_SINGLEPLAYER:
            case RIGHT_OF_SINGLEPLAYER:
               return MCVer.findButton(buttons, "menu.singleplayer", 1);
            case LEFT_OF_MULTIPLAYER:
            case RIGHT_OF_MULTIPLAYER:
               return MCVer.findButton(buttons, "menu.multiplayer", 2);
            case LEFT_OF_REALMS:
            case RIGHT_OF_REALMS:
               return MCVer.findButton(buttons, "menu.online", 14);
            case LEFT_OF_MODS:
            case RIGHT_OF_MODS:
               return MCVer.findButton(buttons, "modmenu.title", 6);
            default:
               throw new RuntimeException();
            }
         }).map((button) -> {
            switch(buttonPosition) {
            case LEFT_OF_SINGLEPLAYER:
            case LEFT_OF_MULTIPLAYER:
            case LEFT_OF_REALMS:
            case LEFT_OF_MODS:
               return new Point(button.getX() - 4 - 20, button.getY());
            case RIGHT_OF_MODS:
            case RIGHT_OF_SINGLEPLAYER:
            case RIGHT_OF_MULTIPLAYER:
            case RIGHT_OF_REALMS:
               return new Point(button.getX() + button.getWidth() + 4, button.getY());
            default:
               throw new RuntimeException();
            }
         }).orElse(topRight);
      }
   }

   private int determineButtonIndex(Collection<AbstractWidget> buttons, AbstractWidget button) {
      AbstractWidget best = null;
      int bestIndex = -1;
      int index = 0;
      Iterator<AbstractWidget> var6 = buttons.iterator();

      while(true) {
         while(var6.hasNext()) {
            AbstractWidget other = var6.next();
            if (other.getY() <= button.getY() && (other.getY() != button.getY() || other.getX() <= button.getX())) {
               if (best == null || other.getY() > best.getY() || other.getY() == best.getY() && other.getX() > best.getX()) {
                  best = other;
                  bestIndex = index + 1;
               }

               ++index;
            } else {
               ++index;
            }
         }

         return bestIndex;
      }
   }

   private void onButton(GuiHandler.InjectedButton button) {
      Screen guiScreen = button.guiScreen;
      if (button.active) {
         if (guiScreen instanceof TitleScreen && button.id == 17890234) {
            (new GuiReplayViewer(this.mod)).display();
         }

         if (guiScreen instanceof PauseScreen && this.mod.getReplayHandler() != null && button.id == 17890235) {
            button.active = false;

            try {
               this.mod.getReplayHandler().endReplay();
            } catch (IOException var4) {
               var4.printStackTrace();
            }
         }

      }
   }

   public static class InjectedButton extends Button {
      public final Screen guiScreen;
      public final int id;
      private final Consumer<GuiHandler.InjectedButton> onClick;

      public InjectedButton(Screen guiScreen, int buttonId, int x, int y, int width, int height, String buttonText, String tooltip, Consumer<GuiHandler.InjectedButton> onClick) {
         super(x, y, width, height, Component.translatable(buttonText), (self) -> {
            onClick.accept((GuiHandler.InjectedButton)self);
         }, DEFAULT_NARRATION);
         this.guiScreen = guiScreen;
         this.id = buttonId;
         this.onClick = onClick;
         if (tooltip != null) {
            this.setTooltip(Tooltip.create(Component.translatable(tooltip)));
         }

      }
   }

   public enum MainMenuButtonPosition {
      BIG,
      DEFAULT,
      TOP_LEFT,
      TOP_RIGHT,
      LEFT_OF_SINGLEPLAYER,
      RIGHT_OF_SINGLEPLAYER,
      LEFT_OF_MULTIPLAYER,
      RIGHT_OF_MULTIPLAYER,
      LEFT_OF_REALMS,
      RIGHT_OF_REALMS,
      LEFT_OF_MODS,
      RIGHT_OF_MODS;

      // $FF: synthetic method
      private static GuiHandler.MainMenuButtonPosition[] $values() {
         return new GuiHandler.MainMenuButtonPosition[]{BIG, DEFAULT, TOP_LEFT, TOP_RIGHT, LEFT_OF_SINGLEPLAYER, RIGHT_OF_SINGLEPLAYER, LEFT_OF_MULTIPLAYER, RIGHT_OF_MULTIPLAYER, LEFT_OF_REALMS, RIGHT_OF_REALMS, LEFT_OF_MODS, RIGHT_OF_MODS};
      }
   }
}
