package net.darktree.glslmc.render;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.darktree.glslmc.PanoramaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.VertexBuffer;
import org.joml.Matrix4f;

import java.io.Closeable;

public class ScalableCanvas implements Closeable {

	private final Matrix4f identity;
	private final Framebuffer input;
	private final Framebuffer output;

	public ScalableCanvas() {
		this.identity = new Matrix4f();
		this.output = MinecraftClient.getInstance().getFramebuffer();
		this.input = new SimpleFramebuffer(output.textureWidth, output.textureHeight, false);
	}

	public void resize(int width, int height) {
		if (width() != width && height() != height && width > 0 && height > 0) {
			input.resize(width, height);
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

	public void read() {
		input.beginRead();
	}

	public void blit(float alpha) {
		output.beginWrite(true);
		input.beginRead();

		RenderSystem.assertOnRenderThreadOrInit();
		GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, input.fbo);
		GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, output.fbo);
		GlStateManager._glBlitFrameBuffer(
				0, 0, input.textureWidth, input.textureHeight,
				0, 0, output.textureWidth, output.textureHeight,
				GlConst.GL_COLOR_BUFFER_BIT, GlConst.GL_NEAREST
		);

		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, output.fbo);
	}

	@Override
	public void close() {
		input.delete();
	}

}
