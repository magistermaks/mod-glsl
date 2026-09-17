package net.darktree.glslmc.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Window;
import net.darktree.glslmc.render.GlobalState;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.settings.Options;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(net.minecraft.client.renderer.CubeMap.class)
public abstract class CubeMapMixin {

	@Unique private double time = 0;
	@Unique private long frame = 0;

	/**
	 * We replace the default cubemap render call with our own,
	 * when Panorama Shades are enabled
	 */
	@WrapMethod(method = "render")
	public void onCubemapRender(float rotXInDegrees, float rotYInDegrees, Operation<Void> original) {
		if (Options.get().enabled) {
			Minecraft client = Minecraft.getInstance();

			Window window = client.getWindow();
			int width = window.getScreenWidth();
			int height = window.getScreenHeight();

			float mx = (float) client.mouseHandler.xpos() / (float) width;
			float my = (float) client.mouseHandler.ypos() / (float) height;

			time += client.getDeltaTracker().getGameTimeDeltaTicks();
			PanoramaRenderer.getInstance().render(client, time / 60, frame, mx, my, width, height);
		} else {
			original.call(rotXInDegrees, rotYInDegrees);
		}

		frame ++;
		GlobalState.nextFrame();
	}

}
