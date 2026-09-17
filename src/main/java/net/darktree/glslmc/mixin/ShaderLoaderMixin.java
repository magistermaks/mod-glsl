package net.darktree.glslmc.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.ShaderType;
import net.darktree.glslmc.PanoramaClient;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.ShaderPatcher;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

@Mixin(ShaderManager.class)
public class ShaderLoaderMixin {

	@Shadow
	private static void loadShader(Identifier id, Resource resource, ShaderType type, Map<Identifier, Resource> resources, ImmutableMap.Builder<?, ?> builder) {
		throw new UnsupportedOperationException();
	}

	@Unique
	private Resource patchShader(Resource resource) {
		try {
			String patched = ShaderPatcher.patch(IOUtils.toString(resource.openAsReader()));

			// re-wrap our shader into a resource to pass it to the vanilla method
			return new Resource(resource.source(), () -> IOUtils.toInputStream(patched, Charset.defaultCharset()));
		} catch (Exception e) {
			return resource; // shouldn't ever happen
		}
	}

	@Unique
	private void injectShader(ImmutableMap.Builder<?, ?> builder, ResourceManager manager, Identifier id, ShaderType type) {
		manager.getResource(id).ifPresent(resource -> loadShader(id, patchShader(resource), type, Collections.emptyMap(), builder));
	}

	/**
	 * Normally this method would mangle our custom shader path
	 * before adding it to the shader cache, we just make it not
	 * do that for all shaders from our namespace.
	 */
	@WrapOperation(
			method = "loadShader",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resources/FileToIdConverter;fileToId(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/resources/Identifier;"
			)
	)
	private static Identifier keepOriginalId(FileToIdConverter instance, Identifier path, Operation<Identifier> original) {
		if (path.getNamespace().equals(PanoramaClient.NAMESPACE)) {
			return path;
		}

		return original.call(instance, path);
	}

	/**
	 * We inject our own shader files into the vanilla machinery here,
	 * we need to do this as they exist in a custom namespace and directory.
	 */
	@WrapOperation(
			method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/client/renderer/ShaderManager$Configs;",
			slice = @Slice(
					to = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/client/renderer/ShaderManager;loadShader(Lnet/minecraft/resources/Identifier;Lnet/minecraft/server/packs/resources/Resource;Lcom/mojang/blaze3d/shaders/ShaderType;Ljava/util/Map;Lcom/google/common/collect/ImmutableMap$Builder;)V"
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;"
			)
	)
	protected ImmutableMap.Builder<?, ?> onShaderPrepare(Operation<ImmutableMap.Builder<?, ?>> original, ResourceManager manager) {
		final ImmutableMap.Builder<?, ?> builder = original.call();

		injectShader(builder, manager, PanoramaRenderer.FRAGMENT_SHADER_ID, ShaderType.FRAGMENT);
		injectShader(builder, manager, PanoramaRenderer.VERTEX_SHADER_ID, ShaderType.VERTEX);

		return builder;
	}

	/**
	 * Once our shaders are loaded (and all the other ones too)
	 * we reload the Panorama Renderer, so that we can verify that the shader is valid and
	 * possibly swap to the fallback renderer
	 */
	@Inject(
			method = "apply(Lnet/minecraft/client/renderer/ShaderManager$Configs;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
			at = @At("TAIL")
	)
	protected void onApplyDone(ShaderManager.Configs definitions, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
		PanoramaRenderer.reload();
	}

}
