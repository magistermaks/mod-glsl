package net.darktree.glslmc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vulkan.VulkanBindGroupLayout;
import com.mojang.blaze3d.vulkan.glsl.IntermediaryShaderModule;
import com.mojang.blaze3d.vulkan.glsl.ShaderCompileException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;

@Mixin(IntermediaryShaderModule.class)
public abstract class IntermediaryShaderModuleMixin {

	@Unique
	private static List<String> getMatchingUniforms(List<VulkanBindGroupLayout.Entry> entries, VulkanBindGroupLayout.VulkanBindGroupEntryType type) {
		return entries.stream()
				.filter(entry -> entry.type() == type)
				.map(VulkanBindGroupLayout.Entry::name)
				.toList();
	}

	@WrapOperation(
			method = "rebind",
			at =  @At(
					value = "NEW",
					args = "class=com/mojang/blaze3d/vulkan/glsl/ShaderCompileException"
			),
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Ljava/util/Set;isEmpty()Z"
					)
			)
	)
	ShaderCompileException addErrorDetails(String message, Operation<ShaderCompileException> original, final List<String> inputs, final List<VulkanBindGroupLayout.Entry> entries) {
		List<String> samplers = getMatchingUniforms(entries, VulkanBindGroupLayout.VulkanBindGroupEntryType.SAMPLED_IMAGE);
		List<String> ubos = getMatchingUniforms(entries, VulkanBindGroupLayout.VulkanBindGroupEntryType.UNIFORM_BUFFER);

		return original.call(message + ". Valid inputs: " + inputs + ", valid samplers: " + samplers + ", valid UBOs: " + ubos);
	}

}
