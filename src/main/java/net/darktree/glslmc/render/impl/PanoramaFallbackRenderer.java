package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

public final class PanoramaFallbackRenderer extends PanoramaRenderer {

	private static final MultiBufferSource.BufferSource IMMEDIATE = MultiBufferSource.immediate(new ByteBufferBuilder(256));
	private static final Component TEXT_TOP = Component.translatable("error.glsl_panorama.top");
	private static final Component TEXT_BOTTOM = Component.translatable("error.glsl_panorama.bottom");

	private final int background;
	private final int foreground;

	public PanoramaFallbackRenderer(int background, int foreground) {
		this.background = background;
		this.foreground = foreground;
	}

	@Override
	public void draw(Minecraft client, double time, long frame, float mouseX, float mouseY, int width, int height, GuiGraphicsExtractor context) {
		Font font = Minecraft.getInstance().font;

		context.fill(0, 0, width, height, background);
		context.text(font, TEXT_TOP, 4, 4, foreground, false);
		context.text(font, TEXT_BOTTOM, 4, 6 + font.lineHeight, foreground, false);
	}

	@Override
	public void close() {

	}

}
