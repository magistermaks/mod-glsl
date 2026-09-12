package net.darktree.glslmc.render;

import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.render.impl.PanoramaFallbackRenderer;
import net.darktree.glslmc.render.impl.PanoramaShaderRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public abstract class PanoramaRenderer {

	public static final Identifier VERTEX_SHADER_ID = PanoramaClient.id("panorama/shader.vert");
	public static final Identifier FRAGMENT_SHADER_ID = PanoramaClient.id("panorama/shader.frag");
	public static final Identifier TEXTURE_ID = PanoramaClient.id("panorama/texture.png");

	private static PanoramaRenderer create() {
		try {
			return new PanoramaShaderRenderer();
		} catch (Exception exception) {
			PanoramaClient.LOGGER.error("Failed to create panorama renderer: {}", exception.getMessage());
		}

		// this will draw a basic error screen and direct the user to read the logs
		return new PanoramaFallbackRenderer(0xFFEF323D, 0xFFFFFFFF);
	}

	private static PanoramaRenderer INSTANCE = null;

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

	/**
	 * Called every frame by from the RotatingCubeMapRenderer Mixin
	 */
	public abstract void draw(MinecraftClient client, double time, long frame, float mouseX, float mouseY, int width, int height, DrawContext context);

	/**
	 * Called when the renderer is removed, to dispose of all the used resources
	 */
	public abstract void close();

}
