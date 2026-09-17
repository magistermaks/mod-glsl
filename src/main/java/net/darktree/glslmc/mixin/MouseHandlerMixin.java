package net.darktree.glslmc.mixin;

import net.darktree.glslmc.render.ButtonAccess;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin implements ButtonAccess {

	@Unique
	boolean rawMouseLeft = false;

	@Unique
	boolean rawMouseRight = false;

	@Override
	public boolean glsl_isLeftPressed() {
		return rawMouseLeft;
	}

	@Override
	public boolean glsl_isRightPressed() {
		return rawMouseRight;
	}

	@Unique
	private void setMouseState(int button, boolean pressed) {
		this.rawMouseLeft = pressed && button == 0;
		this.rawMouseRight = pressed && button == 1;
	}

	@Inject(
			method = "onButton",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/platform/FramerateLimitTracker;onInputReceived()V"
			)
	)
	void recordButton(long handle, MouseButtonInfo info, int action, CallbackInfo ci) {
		setMouseState(info.button(), action == 1);
	}

}
