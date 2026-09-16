package net.darktree.glslmc.settings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ShaderSettingsScreen extends OptionsSubScreen {

	private static final Component NOTE = Component.translatable("screen.glsl_panorama.note").withStyle(ChatFormatting.GRAY);
	private static final Component RELOAD = Component.translatable("screen.glsl_panorama.reload");

	public ShaderSettingsScreen() {
		super(Minecraft.getInstance().screen, Minecraft.getInstance().options, Component.translatable("screen.glsl_panorama.title"));
	}

	@Override
	protected void addTitle() {
		LinearLayout grid = new LinearLayout(width, 0, LinearLayout.Orientation.VERTICAL);
		grid.defaultCellSetting().alignHorizontallyCenter();

		grid.addChild(new StringWidget(title, font));
		grid.addChild(new StringWidget(NOTE, font));

		layout.addToHeader(grid);
	}

	@Override
	protected void addFooter() {
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 + 4, this.height - 27, 150, 20).build());
		this.addRenderableWidget(Button.builder(RELOAD, button -> minecraft.reloadResourcePacks()).bounds(this.width / 2 - 154, this.height - 27, 150, 20).build());
	}

	@Override
	protected void addOptions() {
		this.list.addSmall(Options.ENABLED, Options.QUALITY);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		// Disable quality slider when the shaders are off
		if (list != null && list.findOption(Options.QUALITY) instanceof AbstractWidget widget) {
			widget.active = Options.get().enabled;
		}

		super.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	public void removed() {
		super.removed();
		Options.get().save();
	}

	public void onSpecialKey() {
		if (lastScreen instanceof TitleScreen) {
			onClose();
		}
	}

}
