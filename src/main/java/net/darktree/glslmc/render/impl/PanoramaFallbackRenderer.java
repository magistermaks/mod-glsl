package net.darktree.glslmc.render.impl;

import net.darktree.glslmc.render.PanoramaRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

public final class PanoramaFallbackRenderer extends PanoramaRenderer {

	private static final VertexConsumerProvider.Immediate IMMEDIATE = VertexConsumerProvider.immediate(new BufferAllocator(256));
	private static final Text TEXT_TOP = Text.translatable("error.glsl_panorama.top");
	private static final Text TEXT_BOTTOM = Text.translatable("error.glsl_panorama.bottom");

	private final TextRenderer font;
	private final int background;
	private final int foreground;

	public PanoramaFallbackRenderer(int background, int foreground) {
		this.font = MinecraftClient.getInstance().textRenderer;
		this.background = background;
		this.foreground = foreground;
	}

	@Override
	public void draw(MinecraftClient client, double time, long frame, float mouseX, float mouseY, int width, int height, float alpha) {
		final int argb = ColorHelper.withAlpha(ColorHelper.channelFromFloat(alpha), background);

		DrawContext context = new DrawContext(client, IMMEDIATE);

		context.fill(0, 0, width, height, argb);
		context.drawText(font, TEXT_TOP, 4, 4, foreground, false);
		context.drawText(font, TEXT_BOTTOM, 4, 6 + font.fontHeight, foreground, false);
		context.draw();
	}

	@Override
	public void close() {

	}

}
