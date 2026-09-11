package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
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
import net.minecraft.resource.ResourceManager;
import org.lwjgl.glfw.GLFW;

import java.util.OptionalInt;

public final class PanoramaShaderRenderer extends PanoramaRenderer {

	private final GpuBuffer buffer;
	private final RenderPipeline pipeline;
	private final GpuTexture texture;

	private final ScalableCanvas canvas;

	public PanoramaShaderRenderer() {
		this.canvas = new ScalableCanvas();

		this.pipeline = RenderPipeline.builder()
				.withLocation(PanoramaClient.id("panorama"))
				.withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
				.withVertexShader(VERTEX_SHADER_ID)
				.withFragmentShader(FRAGMENT_SHADER_ID)
				.withUniform("time", UniformType.FLOAT)
				.withUniform("mouse", UniformType.VEC2)
				.withUniform("resolution", UniformType.VEC2)
				.withUniform("frame", UniformType.INT)
				.withUniform("persistent_frame", UniformType.INT)
				.withUniform("speed", UniformType.FLOAT)
				.withUniform("mouse_left_pressed", UniformType.FLOAT)
				.withUniform("mouse_right_pressed", UniformType.FLOAT)
				.withSampler("image")
				.withSampler("backbuffer")
				.build();

		if (!RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
			throw new RuntimeException("Failed to construct pipeline!");
		}

		final MinecraftClient client = MinecraftClient.getInstance();
		final ResourceManager resources = client.getResourceManager();
		final TextureManager textures = client.getTextureManager();

		// check if the image.png was provided
		this.texture = resources.getResource(TEXTURE_ID).isPresent() ? textures.getTexture(TEXTURE_ID).getGlTexture() : null;

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
		final float scale = (float) Options.get().quality;
		final float w = width * scale;
		final float h = height * scale;

		canvas.resize((int) w, (int) h);

		final long window = MinecraftClient.getInstance().getWindow().getHandle();
		boolean left = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) != 0;
		boolean right = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_2) != 0;

		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(canvas.getSurface(), OptionalInt.of(0))) {
			pass.setVertexBuffer(0, buffer);
			pass.setPipeline(pipeline);

			pass.setUniform("time", (float) time);
			pass.setUniform("mouse", mouseX, mouseY);
			pass.setUniform("mouse_left_pressed", left ? 1.0f : 0.0f);
			pass.setUniform("mouse_right_pressed", right ? 1.0f : 0.0f);
			pass.setUniform("resolution", w, h);
			pass.setUniform("frame", frame);
			pass.setUniform("persistent_frame", GlobalState.getFrame());
			pass.setUniform("speed", client.options.getPanoramaSpeed().getValue().floatValue());

			if (texture != null) {
				pass.bindSampler("image", texture);
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
