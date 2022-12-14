package net.darktree.glslmc.mixin;

import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.settings.Options;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RotatingCubeMapRenderer.class)
public abstract class RotatingCubeMapRendererMixin {

	@Unique private float time = 0f;

	@Inject(method = "render", at = @At("HEAD"))
	private void increaseTime(float delta, float alpha, CallbackInfo ci) {
		time += delta;
	}

	@Redirect(method="render", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/CubeMapRenderer;draw(Lnet/minecraft/client/MinecraftClient;FFF)V"))
	public void glsl_render(CubeMapRenderer instance, MinecraftClient client, float x, float y, float alpha) {
		if (Options.get().enabled) {
			Window window = client.getWindow();

			int width = window.getWidth();
			int height = window.getHeight();
			float mx = (float) client.mouse.getX() / (float) width;
			float my = (float) client.mouse.getY() / (float) height;

			PanoramaClient.getRenderer().draw(this.time / 60, mx, my, width, height, alpha);
		} else {
			instance.draw(client, x, y, alpha);
		}
	}

}
