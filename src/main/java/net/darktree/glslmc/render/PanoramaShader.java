package net.darktree.glslmc.render;

import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.render.impl.PanoramaFallbackRenderer;
import net.darktree.glslmc.render.impl.PanoramaShaderRenderer;

public class PanoramaShader {

	private final String vertex;
	private final String fragment;

	public PanoramaShader(String vertex, String fragment) {
		this.vertex = ShaderPatcher.patch(vertex);
		this.fragment = ShaderPatcher.patch(fragment);
	}

	public PanoramaRenderer compile() {
		try {
			return new PanoramaShaderRenderer(vertex, fragment);
		} catch (Exception exception) {
			PanoramaClient.LOGGER.error("Failed to create panorama renderer!", exception);
		}

		return new PanoramaFallbackRenderer(0xEF323D, 0xFFFFFF);
	}

}
