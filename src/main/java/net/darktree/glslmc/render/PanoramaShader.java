package net.darktree.glslmc.render;

import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.render.impl.PanoramaFallbackRenderer;
import net.darktree.glslmc.render.impl.PanoramaShaderRenderer;
import net.minecraft.util.Identifier;

public final class PanoramaShader {

	private final String vertex;
	private final String fragment;
	private final Identifier texture;

	public PanoramaShader(String vertex, String fragment, Identifier texture) {
		this.vertex = ShaderPatcher.patch(vertex);
		this.fragment = ShaderPatcher.patch(fragment);
		this.texture = texture;
	}

	public PanoramaRenderer compile() {
		try {
			return new PanoramaShaderRenderer(vertex, fragment, texture);
		} catch (Exception exception) {
			PanoramaClient.LOGGER.error("Failed to create panorama renderer!", exception);
		}

		return new PanoramaFallbackRenderer(0xEF323D, 0xFFFFFF);
	}

}
