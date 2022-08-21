package net.darktree.glslmc.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class ShaderSettingsScreen extends Screen {

	private final Screen parent;

	public ShaderSettingsScreen() {
		super(new TranslatableText("screen.glsl_panorama.title"));
		parent = MinecraftClient.getInstance().currentScreen;
	}

	@Override
	protected void init() {
		ButtonListWidget options = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
		options.addOptionEntry(Options.ENABLED, Options.QUALITY);

		this.addDrawableChild(options);
		this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> {
			this.client.setScreen(this.parent);
		}));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		DrawableHelper.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5, 0xFFFFFF);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public void onClose() {
		this.client.setScreen(this.parent);
	}

}
