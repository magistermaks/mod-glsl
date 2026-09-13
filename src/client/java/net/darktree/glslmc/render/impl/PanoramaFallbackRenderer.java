package net.darktree.glslmc.render.impl;

import net.darktree.glslmc.render.PanoramaRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.text.Text;

public final class PanoramaFallbackRenderer extends PanoramaRenderer {

	private static final VertexConsumerProvider.Immediate IMMEDIATE = VertexConsumerProvider.immediate(new BufferAllocator(256));
	private static final Text TEXT_TOP = Text.translatable("error.glsl_panorama.top");
	private static final Text TEXT_BOTTOM = Text.translatable("error.glsl_panorama.bottom");

	private final int background;
	private final int foreground;

	public PanoramaFallbackRenderer(int background, int foreground) {
		this.background = background;
		this.foreground = foreground;
	}

	@Override
	public void draw(MinecraftClient client, double time, long frame, float mouseX, float mouseY, int width, int height, DrawContext context) {
		TextRenderer font = MinecraftClient.getInstance().textRenderer;

		context.fill(0, 0, width, height, background);
		context.drawText(font, TEXT_TOP, 4, 4, foreground, false);
		context.drawText(font, TEXT_BOTTOM, 4, 6 + font.fontHeight, foreground, false);
	}

	@Override
	public void close() {

	}

}
