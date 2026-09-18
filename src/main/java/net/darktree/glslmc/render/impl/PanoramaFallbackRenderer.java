package net.darktree.glslmc.render.impl;

import net.darktree.glslmc.render.PanoramaRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class PanoramaFallbackRenderer extends PanoramaRenderer {

	private static final Component TEXT_TOP = Component.translatable("error.glsl_panorama.top");
	private static final Component TEXT_BOTTOM = Component.translatable("error.glsl_panorama.bottom");

	private final int background;
	private final int foreground;
	private final List<Component> text = new ArrayList<>();

	public PanoramaFallbackRenderer(int background, int foreground, List<String> details) {
		this.background = background;
		this.foreground = foreground;

		text.add(TEXT_TOP);
		text.add(TEXT_BOTTOM);
		text.add(Component.empty());

		for (String detail : details) {
			text.add(Component.literal(detail));
		}
	}

	@Override
	public void extract(int width, int height, GuiGraphicsExtractor context) {
		Font font = Minecraft.getInstance().font;

		context.fill(0, 0, width, height, background);

		int y = 4;

		for (Component line : text) {
			context.text(font, line, 4, y, foreground, true);

			y += 2;
			y += font.lineHeight;
		}
	}

}
