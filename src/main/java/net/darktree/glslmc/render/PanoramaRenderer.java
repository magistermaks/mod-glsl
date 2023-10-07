package net.darktree.glslmc.render;

import net.minecraft.client.MinecraftClient;

public interface PanoramaRenderer {

	/**
	 * Called every frame by from the RotatingCubeMapRenderer Mixin
	 */
	void draw(MinecraftClient client, float time, int frame, float mouseX, float mouseY, int width, int height, float alpha);

	/**
	 * Called when the renderer is replaced, to dispose of all the used resources
	 */
	void close();

}
