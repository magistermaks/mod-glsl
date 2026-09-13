package net.darktree.glslmc.mixin;

import net.darktree.glslmc.settings.ShaderSettingsScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

	@Shadow @Final
	private MinecraftClient client;

	/**
	 * We target onInput() here to position our callback roughly
	 * in the right spot, but it's not super important
	 */
	@Inject(
			method="onKey",
			at= @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/option/InactivityFpsLimiter;onInput()V"
			)
	)
	public void glsl_onKey(long window, int action, KeyInput input, CallbackInfo ci) {
		if (input.key() == InputUtil.GLFW_KEY_F5 && action == GLFW.GLFW_RELEASE) {
			Screen current = this.client.currentScreen;

			if (current instanceof ShaderSettingsScreen config) {
				config.onSpecialKey();
			}

			if (current instanceof TitleScreen) {
				this.client.setScreen(new ShaderSettingsScreen());
			}
		}
	}

}
