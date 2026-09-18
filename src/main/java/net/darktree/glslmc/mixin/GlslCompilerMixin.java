package net.darktree.glslmc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.frontend.shaders.GlslCompiler;
import com.mojang.renderpearl.util.ShaderCompileException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlslCompiler.class)
public class GlslCompilerMixin {

	@WrapOperation(
			method = "compileToSpv",
			at = @At(
					value = "NEW",
					args = "class=com/mojang/renderpearl/util/ShaderCompileException"
			)
	)
	ShaderCompileException addErrorDetail(String message, Operation<ShaderCompileException> original, String name) {
		return original.call(name + ": " + message);
	}

}
