package net.darktree.glslmc.render;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.darktree.glslmc.PanoramaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.joml.Matrix4f;

import java.io.Closeable;

public class ScalableCanvas implements Closeable {

	private final Framebuffer input;
	private final Framebuffer output;

	public ScalableCanvas() {
		this.output = MinecraftClient.getInstance().getFramebuffer();
		this.input = new SimpleFramebuffer("panorama", output.textureWidth, output.textureHeight, false);
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

	public Framebuffer getSurface() {
		return input;
	}

	public void blit(float alpha) {

		// we use this only to set the correct framebuffers up then blit ourselves :crying:
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		encoder.copyTextureToTexture(output.getColorAttachment(), input.getColorAttachment(), 0, 0, 0, 0, 0, 0, 0);

		GlStateManager._glBlitFrameBuffer(
				0, 0, input.textureWidth, input.textureHeight,
				0, 0, output.textureWidth, output.textureHeight,
				GlConst.GL_COLOR_BUFFER_BIT, GlConst.GL_NEAREST
		);
	}

	@Override
	public void close() {
		input.delete();
	}

}
