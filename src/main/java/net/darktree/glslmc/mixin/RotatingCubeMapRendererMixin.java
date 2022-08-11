package net.darktree.glslmc.mixin;

import net.darktree.glslmc.PanoramaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RotatingCubeMapRenderer.class)
public class RotatingCubeMapRendererMixin {

	@Shadow private float time;

	@Redirect(method="render", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/CubeMapRenderer;draw(Lnet/minecraft/client/MinecraftClient;FFF)V"))
	public void render(CubeMapRenderer instance, MinecraftClient client, float x, float y, float alpha) {
		Window window = client.getWindow();

		int width = window.getWidth();
		int height = window.getHeight();
		float mx = (float) client.mouse.getX() / (float) width;
		float my = (float) client.mouse.getY() / (float) height;

		PanoramaClient.getRenderer().draw(this.time / 60, mx, my, width, height, alpha);
	}

}
