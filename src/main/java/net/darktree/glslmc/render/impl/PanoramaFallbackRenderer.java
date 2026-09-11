package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

public final class PanoramaFallbackRenderer implements PanoramaRenderer {

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

		Framebuffer framebuffer = client.getFramebuffer();
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		encoder.clearColorAndDepthTextures(framebuffer.getColorAttachment(), argb, framebuffer.getDepthAttachment(), 0.0f);

		DrawContext context = new DrawContext(client, IMMEDIATE);

		context.drawText(font, TEXT_TOP, 4, 4, foreground, false);
		context.drawText(font, TEXT_BOTTOM, 4, 6 + font.fontHeight, foreground, false);
		context.draw();
	}

	@Override
	public void close() {

	}

}
