package net.darktree.glslmc.render;

import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.render.impl.PanoramaFallbackRenderer;
import net.darktree.glslmc.render.impl.PanoramaShaderRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PanoramaRenderer {

	public static final Identifier VERTEX_SHADER_ID = PanoramaClient.id("panorama/shader.vert");
	public static final Identifier FRAGMENT_SHADER_ID = PanoramaClient.id("panorama/shader.frag");
	public static final Identifier TEXTURE_ID = PanoramaClient.id("panorama/texture.png");

	private static final List<String> DETAILS = new ArrayList<>();
	private static PanoramaRenderer INSTANCE = null;

	private static PanoramaRenderer instantiate() {
		try {
			return new PanoramaShaderRenderer();
		} catch (Exception exception) {
			PanoramaClient.LOGGER.error("Failed to create panorama renderer: {}", exception.getMessage());
		}

		// this will draw a basic error screen and direct the user to read the logs
		return new PanoramaFallbackRenderer(0xFFEF323D, 0xFFFFFFFF, DETAILS);
	}

	private static PanoramaRenderer create() {
		PanoramaRenderer renderer = instantiate();
		DETAILS.clear();
		return renderer;
	}

	public static void reload() {
		if (INSTANCE != null) {
			INSTANCE.close();
			INSTANCE = null;
		}
	}

	public static PanoramaRenderer getInstance() {
		if (INSTANCE == null) {
			INSTANCE = create();
		}

		return INSTANCE;
	}

	public static void addDebugInfo(String info) {
		Collections.addAll(DETAILS, info.split("\n"));
	}

	/**
	 * Called every frame by from the CubeMap Mixin
	 */
	public void render(Minecraft client, double time, long frame, float mouseX, float mouseY, int width, int height) {
		// do nothing
	}

	/**
	 * Can be used to draw a GUI overlay over the screen
	 */
	public void extract(int width, int height, GuiGraphicsExtractor context) {
		// do nothing
	}

	/**
	 * Called when the renderer is removed, to dispose of all the used resources
	 */
	public void close() {
		// do nothing
	}

}
