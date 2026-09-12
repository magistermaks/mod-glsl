package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.buffers.*;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.render.GlobalState;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.ScalableCanvas;
import net.darktree.glslmc.settings.Options;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.glfw.GLFW;

import java.util.OptionalInt;

public final class PanoramaShaderRenderer extends PanoramaRenderer {

	private static final int UBO_SIZE = new Std140SizeCalculator()
			.putFloat() // time
			.putVec2()  // mouse
			.putVec2()  // resolution
			.putInt()   // frame
			.putInt()   // persistent_frame
			.putFloat() // speed
			.putFloat() // mouse_left_pressed
			.putFloat() // mouse_right_pressed
			.get();

	private final GpuBuffer vbo;
	private final RenderPipeline pipeline;
	private final GpuTextureView texture;
	private final MappableRingBuffer ubo;

	private final ScalableCanvas canvas;
	private final ScalableCanvas backbuffer;

	public PanoramaShaderRenderer() {
		this.canvas = new ScalableCanvas();
		this.backbuffer = new ScalableCanvas();
		this.ubo = new MappableRingBuffer(() -> "Panorama UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, UBO_SIZE);

		this.pipeline = RenderPipeline.builder()
				.withLocation(PanoramaClient.id("panorama"))
				.withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
				.withVertexShader(VERTEX_SHADER_ID)
				.withFragmentShader(FRAGMENT_SHADER_ID)
				.withUniform("info", UniformType.UNIFORM_BUFFER)
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
		this.texture = resources.getResource(TEXTURE_ID).isPresent() ? textures.getTexture(TEXTURE_ID).getGlTextureView() : null;

		// bake buffer data
		BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE_COLOR);
		builder.vertex(-1.0f, -1.0f,  1.0f).texture(0, 0).color(1f, 1f, 1f, 1f);
		builder.vertex( 1.0f, -1.0f,  1.0f).texture(1, 0).color(1f, 1f, 1f, 1f);
		builder.vertex( 1.0f,  1.0f,  1.0f).texture(1, 1).color(1f, 1f, 1f, 1f);
		builder.vertex(-1.0f, -1.0f,  1.0f).texture(0, 0).color(1f, 1f, 1f, 1f);
		builder.vertex( 1.0f,  1.0f,  1.0f).texture(1, 1).color(1f, 1f, 1f, 1f);
		builder.vertex(-1.0f,  1.0f,  1.0f).texture(0, 1).color(1f, 1f, 1f, 1f);

		try (BuiltBuffer built = builder.end()) {
			this.vbo = RenderSystem.getDevice().createBuffer(() -> "Panorama Quad", GpuBuffer.USAGE_VERTEX, built.getBuffer());
		}
	}

	@Override
	public void draw(MinecraftClient client, double time, long frame, float mouseX, float mouseY, int width, int height, DrawContext context) {
		final float scale = (float) Options.get().quality;
		final float w = width * scale;
		final float h = height * scale;

		canvas.resize((int) w, (int) h);

		final long window = MinecraftClient.getInstance().getWindow().getHandle();
		boolean left = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) != 0;
		boolean right = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_2) != 0;

		Framebuffer target = MinecraftClient.getInstance().getFramebuffer();
		GpuDevice device = RenderSystem.getDevice();

		try (GpuBuffer.MappedView view = device.createCommandEncoder().mapBuffer(ubo.getBlocking(), false, true)) {
			Std140Builder.intoBuffer(view.data())
				.putFloat((float) time)
				.putVec2(mouseX, mouseY)
				.putVec2(w, h)
				.putInt((int) frame)
				.putInt(GlobalState.getFrame())
				.putFloat(client.options.getPanoramaSpeed().getValue().floatValue())
				.putFloat(left ? 1.0f : 0.0f)
				.putFloat(right ? 1.0f : 0.0f);
		}

		try (RenderPass pass = device.createCommandEncoder().createRenderPass(() -> "Panorama", canvas.getColorView(), OptionalInt.of(0))) {
			pass.setVertexBuffer(0, vbo);
			pass.setPipeline(pipeline);

			pass.setUniform("info", this.ubo.getBlocking());
			pass.bindSampler("image", texture);
			pass.bindSampler("backbuffer", backbuffer.getColorView());

			pass.draw(0, 6);
		}

		backbuffer.resize((int) w, (int) h);

		canvas.blitInto(backbuffer.getColorView());
		canvas.blitInto(target.getColorAttachmentView());
	}

	@Override
	public void close() {
		vbo.close();
		ubo.close();
		canvas.close();
	}

}
