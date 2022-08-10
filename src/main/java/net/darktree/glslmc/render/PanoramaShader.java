package net.darktree.glslmc.render;

import com.mojang.blaze3d.systems.RenderSystem;

public class PanoramaShader {

	private final String vertex;
	private final String fragment;
	private PanoramaRenderer renderer = null;

	public PanoramaShader(String vertex, String fragment) {
		this.vertex = ShaderPatcher.patch(vertex);
		this.fragment = ShaderPatcher.patch(fragment);
	}

	public void close() {
		RenderSystem.recordRenderCall(() -> {
			renderer.close();
		});
	}

	public PanoramaRenderer getRenderer() {
		if (renderer == null) {
			renderer = new PanoramaRenderer(vertex, fragment);
		}

		return renderer;
	}

}
