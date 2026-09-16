package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.render.GlobalState;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.ScalableCanvas;
import net.darktree.glslmc.settings.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManager;
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
				.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.TRIANGLES)
				.withVertexShader(VERTEX_SHADER_ID)
				.withFragmentShader(FRAGMENT_SHADER_ID)
				.withUniform("info", UniformType.UNIFORM_BUFFER)
				.withSampler("image")
				.withSampler("backbuffer")
				.build();

		if (!RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
			throw new RuntimeException("Failed to construct pipeline!");
		}

		final Minecraft client = Minecraft.getInstance();
		final ResourceManager resources = client.getResourceManager();
		final TextureManager textures = client.getTextureManager();

		// check if the image.png was provided
		this.texture = resources.getResource(TEXTURE_ID).isPresent() ? textures.getTexture(TEXTURE_ID).getTextureView() : null;

		// bake buffer data
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
		builder.addVertex(-1.0f, -1.0f,  1.0f).setUv(0, 0).setColor(1f, 1f, 1f, 1f);
		builder.addVertex( 1.0f, -1.0f,  1.0f).setUv(1, 0).setColor(1f, 1f, 1f, 1f);
		builder.addVertex( 1.0f,  1.0f,  1.0f).setUv(1, 1).setColor(1f, 1f, 1f, 1f);
		builder.addVertex(-1.0f, -1.0f,  1.0f).setUv(0, 0).setColor(1f, 1f, 1f, 1f);
		builder.addVertex( 1.0f,  1.0f,  1.0f).setUv(1, 1).setColor(1f, 1f, 1f, 1f);
		builder.addVertex(-1.0f,  1.0f,  1.0f).setUv(0, 1).setColor(1f, 1f, 1f, 1f);

		try (MeshData built = builder.buildOrThrow()) {
			this.vbo = RenderSystem.getDevice().createBuffer(() -> "Panorama Quad", GpuBuffer.USAGE_VERTEX, built.vertexBuffer());
		}
	}

	@Override
	public void render(Minecraft client, double time, long frame, float mouseX, float mouseY, int width, int height) {
		final float scale = (float) Options.get().quality;
		final float w = width * scale;
		final float h = height * scale;

		canvas.resize((int) w, (int) h);

		final long window = Minecraft.getInstance().getWindow().handle();
		boolean left = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) != 0;
		boolean right = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_2) != 0;

		RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
		GpuDevice device = RenderSystem.getDevice();
		GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);

		try (GpuBuffer.MappedView view = device.createCommandEncoder().mapBuffer(ubo.currentBuffer(), false, true)) {
			Std140Builder.intoBuffer(view.data())
				.putFloat((float) time)
				.putVec2(mouseX, mouseY)
				.putVec2(w, h)
				.putInt((int) frame)
				.putInt(GlobalState.getFrame())
				.putFloat(client.options.panoramaSpeed().get().floatValue())
				.putFloat(left ? 1.0f : 0.0f)
				.putFloat(right ? 1.0f : 0.0f);
		}

		try (RenderPass pass = device.createCommandEncoder().createRenderPass(() -> "Panorama", canvas.getColorView(), OptionalInt.of(0))) {
			pass.setVertexBuffer(0, vbo);
			pass.setPipeline(pipeline);

			pass.setUniform("info", this.ubo.currentBuffer());
			pass.bindTexture("image", texture, sampler);
			pass.bindTexture("backbuffer", backbuffer.getColorView(), sampler);

			pass.draw(0, 6);
		}

		backbuffer.resize((int) w, (int) h);

		canvas.blitInto(backbuffer.getColorView());
		canvas.blitInto(target.getColorTextureView());
	}

	@Override
	public void close() {
		vbo.close();
		ubo.close();
		canvas.close();
	}

}
