package net.darktree.glslmc.mixin;

import net.darktree.glslmc.render.GlobalState;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class WindowMixin {

	@Inject(method = "swapBuffers", at = @At("HEAD"))
	public void glsl_swapBuffers(CallbackInfo ci) {
		GlobalState.swapFrame();
	}

}
