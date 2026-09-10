package net.darktree.glslmc.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ShaderSettingsScreen extends GameOptionsScreen {

	private static final Text NOTE = Text.translatable("screen.glsl_panorama.note").formatted(Formatting.GRAY);
	private static final Text RELOAD = Text.translatable("screen.glsl_panorama.reload");

	public ShaderSettingsScreen() {
		super(MinecraftClient.getInstance().currentScreen, MinecraftClient.getInstance().options, Text.translatable("screen.glsl_panorama.title"));
	}

	@Override
	protected void init() {
		OptionListWidget options = new OptionListWidget(this.client, this.width, this);
		options.addAll(Options.ENABLED, Options.QUALITY);

		this.addDrawableChild(options);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).dimensions(this.width / 2 + 4, this.height - 27, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(RELOAD, button -> client.reloadResources()).dimensions(this.width / 2 - 154, this.height - 27, 150, 20).build());
	}

	@Override
	protected void addOptions() {

	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, NOTE, this.width / 2, 20, 0xFFFFFF);
	}

	@Override
	public void removed() {
		super.removed();
		Options.get().save();
	}

}
