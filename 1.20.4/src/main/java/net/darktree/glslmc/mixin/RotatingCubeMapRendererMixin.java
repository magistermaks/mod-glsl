package net.darktree.glslmc.mixin;

import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.render.GlobalState;
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

	@Unique private double time = 0f;
	@Unique private long frame = 0;

	@Inject(method = "render", at = @At("HEAD"))
	private void increaseTime(float delta, float alpha, CallbackInfo ci) {
		time += delta;
		frame += 1;
	}

	@Redirect(method="render", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/CubeMapRenderer;draw(Lnet/minecraft/client/MinecraftClient;FFF)V"))
	public void glsl_render(CubeMapRenderer instance, MinecraftClient client, float x, float y, float alpha) {
		if (Options.get().enabled) {
			Window window = client.getWindow();

			int width = window.getWidth();
			int height = window.getHeight();
			float mx = (float) client.mouse.getX() / (float) width;
			float my = (float) client.mouse.getY() / (float) height;

			PanoramaClient.getRenderer().draw(client, this.time / 60, frame, mx, my, width, height, alpha);
		} else {
			instance.draw(client, x, y, alpha);
		}

		GlobalState.nextFrame();
	}

}
