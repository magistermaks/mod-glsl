package net.darktree.glslmc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.darktree.glslmc.render.GlobalState;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.settings.Options;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RotatingCubeMapRenderer.class)
public abstract class RotatingCubeMapRendererMixin {

	@Unique private double time = 0f;
	@Unique private long frame = 0;

	/**
	 * We replace the default cubemap render call with our own,
	 * when Panorama Shades are enabled
	 */
	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/CubeMapRenderer;draw(Lnet/minecraft/client/MinecraftClient;FFF)V"
			)
	)
	public void onCubemapDraw(CubeMapRenderer instance, MinecraftClient client, float x, float y, float a, Operation<Void> original, DrawContext context, int width, int height, float alpha, float delta) {
		time += delta;
		frame += 1;

		if (Options.get().enabled) {
			Window window = MinecraftClient.getInstance().getWindow();
			width = window.getWidth();
			height = window.getHeight();

			float mx = (float) client.mouse.getX() / (float) width;
			float my = (float) client.mouse.getY() / (float) height;

			PanoramaRenderer.getInstance().draw(client, this.time / 60, frame, mx, my, width, height, a);
		} else {
			original.call(instance, client, x, y, a);
		}

		GlobalState.nextFrame();
	}

}
