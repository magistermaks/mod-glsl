package net.darktree.glslmc.render;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;

import java.io.Closeable;

public class ScalableCanvas implements Closeable {

	private final Framebuffer input;

	public ScalableCanvas() {
		Framebuffer output = MinecraftClient.getInstance().getFramebuffer();
		this.input = new SimpleFramebuffer("panorama", output.textureWidth, output.textureHeight, false);
	}

	public void resize(int width, int height) {
		if (width > 0 && height > 0) {
			input.resize(width, height);
		}
	}

	public GpuTextureView getColorView() {
		return input.getColorAttachmentView();
	}

	public void blitInto(GpuTextureView view) {
		input.drawBlit(view);
	}

	@Override
	public void close() {
		input.delete();
	}

}
