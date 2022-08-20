package net.darktree.glslmc.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

	@Shadow @Final private MinecraftClient client;

	@Inject(method="onKey", at=@At("TAIL"))
	public void glsl_onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {

		// force resource reload from the title screen
		// helpfully when making/debugging shaders
		if (key == InputUtil.GLFW_KEY_F5 && action == GLFW.GLFW_RELEASE && this.client.currentScreen instanceof TitleScreen) {
			this.client.reloadResources();
		}

	}

}
