package net.darktree.glslmc.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.*;
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
	protected void initHeader() {
		DirectionalLayoutWidget grid = new DirectionalLayoutWidget(width, 0, DirectionalLayoutWidget.DisplayAxis.VERTICAL);
		grid.getMainPositioner().alignHorizontalCenter();

		grid.add(new TextWidget(title, textRenderer));
		grid.add(new TextWidget(NOTE, textRenderer));

		layout.addHeader(grid);
	}

	@Override
	protected void initFooter() {
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).dimensions(this.width / 2 + 4, this.height - 27, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(RELOAD, button -> client.reloadResources()).dimensions(this.width / 2 - 154, this.height - 27, 150, 20).build());
	}

	@Override
	protected void addOptions() {
		this.body.addAll(Options.ENABLED, Options.QUALITY);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		// Disable quality slider when the shaders are off
		if (body != null && body.getWidgetFor(Options.QUALITY) instanceof ClickableWidget widget) {
			widget.active = Options.get().enabled;
		}

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, NOTE, this.width / 2, 20, 0xFFFFFF);

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void removed() {
		super.removed();
		Options.get().save();
	}

	public void onSpecialKey() {
		if (parent instanceof TitleScreen) {
			close();
		}
	}

}
