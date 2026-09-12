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

	@Unique private long frame = 0;

	/**
	 * We replace the default cubemap render call with our own,
	 * when Panorama Shades are enabled
	 */
	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/CubeMapRenderer;draw(Lnet/minecraft/client/MinecraftClient;FF)V"
			)
	)
	public void onCubemapDraw(CubeMapRenderer instance, MinecraftClient client, float x, float y, Operation<Void> original, DrawContext context) {
		if (Options.get().enabled) {
			Window window = client.getWindow();
			int width = window.getWidth();
			int height = window.getHeight();

			float mx = (float) client.mouse.getX() / (float) width;
			float my = (float) client.mouse.getY() / (float) height;

			float time = client.getRenderTickCounter().getFixedDeltaTicks();
			PanoramaRenderer.getInstance().draw(client, time / 60, frame, mx, my, width, height, context);
		} else {
			original.call(instance, client, x, y);
		}

		GlobalState.nextFrame();
	}

}
