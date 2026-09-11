package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.render.GlobalState;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.ScalableCanvas;
import net.darktree.glslmc.settings.Options;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.util.OptionalInt;

public final class PanoramaShaderRenderer extends PanoramaRenderer {

	private final GpuBuffer buffer;
	private final RenderPipeline pipeline;
	private final Identifier texture;

	private final ScalableCanvas canvas;
	private final TextureManager manager;

	public PanoramaShaderRenderer() {
		this.canvas = new ScalableCanvas();
		this.manager = MinecraftClient.getInstance().getTextureManager();

		this.pipeline = RenderPipeline.builder()
				.withLocation(PanoramaClient.id("panorama"))
				.withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
				.withVertexShader(VERTEX_SHADER_ID)
				.withFragmentShader(FRAGMENT_SHADER_ID)
				.withUniform("time", UniformType.FLOAT)
				.withUniform("mouse", UniformType.VEC2)
				.withUniform("resolution", UniformType.VEC2)
				.withUniform("image", UniformType.INT)
				.withUniform("backbuffer", UniformType.INT)
				.withUniform("frame", UniformType.INT)
				.withUniform("persistent_frame", UniformType.INT)
				.withUniform("speed", UniformType.FLOAT)
				.build();

		if (!RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
			throw new RuntimeException("Failed to construct pipeline!");
		}

		this.texture = null; // TODO

		// bake buffer data
		BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);
		builder.vertex(-1.0f, -1.0f,  1.0f).texture(0, 0).color(1f, 1f, 1f, 1f);
		builder.vertex( 1.0f, -1.0f,  1.0f).texture(1, 0).color(1f, 1f, 1f, 1f);
		builder.vertex( 1.0f,  1.0f,  1.0f).texture(1, 1).color(1f, 1f, 1f, 1f);
		builder.vertex(-1.0f, -1.0f,  1.0f).texture(0, 0).color(1f, 1f, 1f, 1f);
		builder.vertex( 1.0f,  1.0f,  1.0f).texture(1, 1).color(1f, 1f, 1f, 1f);
		builder.vertex(-1.0f,  1.0f,  1.0f).texture(0, 1).color(1f, 1f, 1f, 1f);

		try (BuiltBuffer built = builder.end()) {
			this.buffer = RenderSystem.getDevice().createBuffer(() -> "panorama surface", BufferType.VERTICES, BufferUsage.STATIC_WRITE, built.getBuffer());
		}
	}

	@Override
	public void draw(MinecraftClient client, double time, long frame, float mouseX, float mouseY, int width, int height, float alpha) {
		float scale = (float) Options.get().quality;
		canvas.resize((int) (width * scale), (int) (height * scale));

		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(canvas.getSurface(), OptionalInt.of(0))) {
			pass.setVertexBuffer(0, buffer);
			pass.setPipeline(pipeline);

			pass.setUniform("time", (float) time);
			pass.setUniform("mouse", mouseX, mouseY);
			pass.setUniform("resolution", (float) canvas.width(), (float) canvas.height());
			pass.setUniform("frame", frame);
			pass.setUniform("persistent_frame", GlobalState.getFrame());
			pass.setUniform("speed",  client.options.getPanoramaSpeed().getValue().floatValue());

			if (texture != null) {
				pass.bindSampler("image", manager.getTexture(texture).getGlTexture());
			}

			// TODO pass.bindSampler("backbuffer", );

			pass.draw(0, 6);
		}

		canvas.blit(alpha);
	}

	@Override
	public void close() {
		buffer.close();
		canvas.close();
	}

}
