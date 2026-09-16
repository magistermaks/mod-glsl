package net.darktree.glslmc.mixin;

import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.settings.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Panorama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Panorama.class)
public class PanoramaMixin {

	@Inject(
			method = "extractRenderState",
			at = @At("TAIL")
	)
	public void onExtractRenderState(GuiGraphicsExtractor graphics, int width, int height, boolean shouldSpin, CallbackInfo ci) {
		if (Options.get().enabled) {
			PanoramaRenderer.getInstance().extract(width, height, graphics);
		}
	}

}
