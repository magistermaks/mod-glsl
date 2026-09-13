package net.darktree.glslmc.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.io.Closeable;
import net.minecraft.client.Minecraft;

public class ScalableCanvas implements Closeable {

	private final RenderTarget input;

	public ScalableCanvas() {
		RenderTarget output = Minecraft.getInstance().getMainRenderTarget();
		this.input = new TextureTarget("panorama", output.width, output.height, false);
	}

	public void resize(int width, int height) {
		if (width > 0 && height > 0) {
			input.resize(width, height);
		}
	}

	public GpuTextureView getColorView() {
		return input.getColorTextureView();
	}

	public void blitInto(GpuTextureView view) {
		input.blitAndBlendToTexture(view);
	}

	@Override
	public void close() {
		input.destroyBuffers();
	}

}
