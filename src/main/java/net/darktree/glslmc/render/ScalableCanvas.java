package net.darktree.glslmc.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.darktree.glslmc.PanoramaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Matrix4f;

import java.io.Closeable;

public class ScalableCanvas implements Closeable {

	private final Matrix4f identity;
	private final Framebuffer input;
	private final Framebuffer output;

	public ScalableCanvas() {
		this.identity = new Matrix4f();
		this.identity.loadIdentity();
		this.output = MinecraftClient.getInstance().getFramebuffer();
		this.input = new SimpleFramebuffer(output.textureWidth, output.textureHeight, false, false);
	}

	public void resize(int width, int height) {
		if (width() != width && height() != height) {
			input.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
			PanoramaClient.LOGGER.info("Resized shader canvas to " + width + "x" + height);
		}
	}

	public int width() {
		return input.textureWidth;
	}

	public int height() {
		return input.textureHeight;
	}

	public void write() {
		input.beginWrite(true);
	}

	public void blit(VertexBuffer buffer, float alpha) {
		output.beginWrite(true);

		RenderSystem.setShaderTexture(0, input.getColorAttachment());
		RenderSystem.setShaderColor(1, 1, 1, alpha);

		RenderSystem.enableBlend();
		buffer.setShader(identity, identity, GameRenderer.getPositionTexColorShader());
	}

	@Override
	public void close() {
		input.delete();
	}

}
