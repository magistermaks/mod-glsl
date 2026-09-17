package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.*;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.render.ButtonAccess;
import net.darktree.glslmc.render.GlobalState;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.ScalableCanvas;
import net.darktree.glslmc.settings.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.Optional;

public final class PanoramaShaderRenderer extends PanoramaRenderer {

	private static final Optional<Vector4fc> CLEAR_COLOR = Optional.of(new Vector4f(0, 0, 0, 1));

	public static final BindGroupLayout UNIFORM_LAYOUT = BindGroupLayout.builder()
			.withUniform("info", UniformType.UNIFORM_BUFFER)
			.withUniform("image", UniformType.COMBINED_IMAGE_SAMPLER)
			.withUniform("backbuffer", UniformType.COMBINED_IMAGE_SAMPLER)
			.build();

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
	private final CompiledRenderPipeline pipeline;
	private final GpuTextureView texture;
	private final MappableRingBuffer ubo;

	private final ScalableCanvas canvas;
	private final ScalableCanvas backbuffer;

	private AbstractTexture getTexture(Identifier identifier) {
		Minecraft client = Minecraft.getInstance();
		ResourceManager resources = client.getResourceManager();
		TextureManager textures = client.getTextureManager();

		if (resources.getResource(identifier).isPresent()) {
			return textures.getTexture(identifier);
		} else {
			SimpleTexture texture = new SimpleTexture(identifier);
			texture.apply(TextureContents.createMissing());
			return texture;
		}
	}

	public PanoramaShaderRenderer() {
		this.canvas = new ScalableCanvas();
		this.backbuffer = new ScalableCanvas();
		this.ubo = new MappableRingBuffer(() -> "Panorama UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, UBO_SIZE);

		RenderPipeline pipeline = RenderPipeline.builder()
				.withLocation(PanoramaClient.id("panorama"))
				.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
				.withVertexShader(VERTEX_SHADER_ID)
				.withFragmentShader(FRAGMENT_SHADER_ID)
				.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
				.withBindGroupLayout(UNIFORM_LAYOUT)
				.withColorTargetState(ColorTargetState.DEFAULT)
				.build();

		this.pipeline = RenderSystem.getCompiledPipeline(pipeline);

		// check if the image.png was provided
		this.texture = getTexture(TEXTURE_ID).getTextureView();

		// bake buffer data
		try (ByteBufferBuilder buffer = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize() * 6)) {
			BufferBuilder builder = new BufferBuilder(buffer, PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
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

		// clear backbuffer
		RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Clear Backbuffer", backbuffer.getColorView(), CLEAR_COLOR).close();
	}

	@Override
	public void render(Minecraft client, double time, long frame, float mouseX, float mouseY, int width, int height) {
		final float scale = (float) Options.get().quality;
		final float w = width * scale;
		final float h = height * scale;

		canvas.resize((int) w, (int) h);

		ButtonAccess button = ((ButtonAccess) client.mouseHandler);
		boolean left = button.glsl_isLeftPressed();
		boolean right = button.glsl_isRightPressed();

		RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuDevice device = RenderSystem.getDevice();
		GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);

		try (GpuBufferSlice.MappedView view = ubo.currentBuffer().map(false, true)) {
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

		try (RenderPass pass = device.createCommandEncoder().createRenderPass(() -> "Panorama", canvas.getColorView(), CLEAR_COLOR)) {
			pass.setVertexBuffer(0, vbo.slice());
			pass.setPipeline(pipeline);

			pass.setUniform("info", this.ubo.currentBuffer());
			pass.setUniform("image", texture, sampler);
			pass.setUniform("backbuffer", backbuffer.getColorView(), sampler);

			pass.draw(6, 1, 0, 0);
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
