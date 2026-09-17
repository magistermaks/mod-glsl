package net.darktree.glslmc.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.darktree.glslmc.settings.ShaderSettingsScreen;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardMixin {

	@Shadow @Final
	private Minecraft minecraft;

	/**
	 * We target onInput() here to position our callback roughly
	 * in the right spot, but it's not super important
	 */
	@Inject(
			method="keyPress",
			at= @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/platform/FramerateLimitTracker;onInputReceived()V"
			)
	)
	public void glsl_onKey(long window, int action, KeyEvent input, CallbackInfo ci) {
		if (input.key() == InputConstants.KEY_F5 && action == GLFW.GLFW_RELEASE) {
			Screen current = this.minecraft.screen;

			if (current instanceof ShaderSettingsScreen config) {
				config.onSpecialKey();
			}

			if (current instanceof TitleScreen) {
				this.minecraft.setScreen(new ShaderSettingsScreen());
			}
		}
	}

}
