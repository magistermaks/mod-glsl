package net.darktree.glslmc.mixin;

import com.mojang.blaze3d.shaders.ShaderType;
import net.darktree.glslmc.PanoramaClient;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gl.ShaderLoader$Cache")
public abstract class ShaderLoaderCacheMixin {

	/**
	 * This whole approach is soo ass but allows use to maintain the same pack format, and is quite simple
	 * in its own roundabout way.
	 *
	 * PanoramaResourceLoader sets the PanoramaClient.shader that is used to create the renderer instance (based on compilation status)
	 * but to create a RenderPipeline we need not the shader source but the path, and minecraft will only allow shaders from the
	 * shader directory. We don't add our shader to the shader source cache as that would place a dependency between resource loader order
	 */
	@Inject(method = "getSource", at = @At("HEAD"), cancellable = true)
	public void getSource(Identifier id, ShaderType type, CallbackInfoReturnable<String> cir) {
		if (PanoramaClient.getSource(id) instanceof String source) {
			cir.setReturnValue(source);
		}
	}

}
