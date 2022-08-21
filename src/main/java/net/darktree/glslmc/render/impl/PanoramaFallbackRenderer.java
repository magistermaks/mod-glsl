package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public final class PanoramaFallbackRenderer implements PanoramaRenderer {

	private static final MatrixStack IDENTITY = new MatrixStack();
	private static final Text TEXT_TOP = new TranslatableText("error.glsl_panorama.top");
	private static final Text TEXT_BOTTOM = new TranslatableText("error.glsl_panorama.bottom");

	private final TextRenderer font;
	private final float r, g, b;
	private final int foreground;

	public PanoramaFallbackRenderer(int background, int foreground) {
		this.font = MinecraftClient.getInstance().textRenderer;

		this.r = BackgroundHelper.ColorMixer.getRed(background) / 255.0f;
		this.g = BackgroundHelper.ColorMixer.getGreen(background) / 255.0f;
		this.b = BackgroundHelper.ColorMixer.getBlue(background) / 255.0f;
		this.foreground = foreground;
	}

	@Override
	public void draw(float time, float mouseX, float mouseY, int width, int height, float alpha) {
		RenderSystem.clearColor(r, g, b, alpha);
		RenderSystem.clear(GlConst.GL_COLOR_BUFFER_BIT | GlConst.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

		font.draw(IDENTITY, TEXT_TOP, 4, 4, foreground);
		font.draw(IDENTITY, TEXT_BOTTOM, 4, 6 + font.fontHeight, foreground);
	}

	@Override
	public void close() {

	}

}
