package net.darktree.glslmc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import net.darktree.glslmc.render.GlobalState;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.settings.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CubeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(net.minecraft.client.renderer.PanoramaRenderer.class)
public abstract class RotatingCubeMapRendererMixin {

	@Unique private double time = 0;
	@Unique private long frame = 0;

	/**
	 * We replace the default cubemap render call with our own,
	 * when Panorama Shades are enabled
	 */
	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/CubeMap;render(Lnet/minecraft/client/Minecraft;FF)V"
			)
	)
	public void onCubemapDraw(CubeMap instance, Minecraft client, float x, float y, Operation<Void> original, GuiGraphics context) {
		if (Options.get().enabled) {
			Window window = client.getWindow();
			int width = window.getScreenWidth();
			int height = window.getScreenHeight();

			float mx = (float) client.mouseHandler.xpos() / (float) width;
			float my = (float) client.mouseHandler.ypos() / (float) height;

			time += client.getDeltaTracker().getGameTimeDeltaTicks();
			PanoramaRenderer.getInstance().draw(client, time / 60, frame, mx, my, width, height, context);
		} else {
			original.call(instance, client, x, y);
		}

		GlobalState.nextFrame();
	}

}
