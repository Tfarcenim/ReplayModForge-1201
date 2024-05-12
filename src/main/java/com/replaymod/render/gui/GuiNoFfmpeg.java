package com.replaymod.render.gui;

import com.replaymod.core.versions.MCVer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;

import java.net.URI;

public class GuiNoFfmpeg extends GuiScreen {
   private static final String LINK = "https://www.replaymod.com/docs/#installing-ffmpeg";
   private final GuiLabel message = new GuiLabel().setI18nText("replaymod.gui.rendering.error.message");
   private final GuiLabel link = new GuiLabel().setText("https://www.replaymod.com/docs/#installing-ffmpeg");
   private final GuiButton openLinkButton = new GuiButton().setI18nLabel("chat.link.open", new Object[0]).setSize(100, 20).onClick(() -> {
      MCVer.openURL(URI.create("https://www.replaymod.com/docs/#installing-ffmpeg"));
   });
   private final GuiButton copyToClipboardButton = new GuiButton().setI18nLabel("chat.copy", new Object[0]).setSize(100, 20).onClick(() -> {
      com.replaymod.lib.de.johni0702.minecraft.gui.versions.MCVer.setClipboardString("https://www.replaymod.com/docs/#installing-ffmpeg");
   });
   private final GuiButton backButton = new GuiButton().setI18nLabel("gui.back", new Object[0]).setSize(100, 20);
   private final GuiPanel buttons;

   public GuiNoFfmpeg(Runnable goBack) {
      this.buttons = new GuiPanel().setLayout(new HorizontalLayout(HorizontalLayout.Alignment.CENTER).setSpacing(4)).addElements(null, this.openLinkButton, this.copyToClipboardButton, this.backButton);
      this.setBackground(AbstractGuiScreen.Background.DIRT);
      this.setTitle(new GuiLabel().setI18nText("replaymod.gui.rendering.error.title"));
      this.setLayout(new VerticalLayout(VerticalLayout.Alignment.CENTER).setSpacing(30));
      this.addElements(new VerticalLayout.Data(0.5D), this.message, this.link, this.buttons);
      this.backButton.onClick(goBack);
   }
}
