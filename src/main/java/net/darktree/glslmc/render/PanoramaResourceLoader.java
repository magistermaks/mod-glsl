package net.darktree.glslmc.render;

import net.darktree.glslmc.PanoramaClient;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PanoramaResourceLoader implements SimpleResourceReloadListener<PanoramaShader> {

	@Override
	public Identifier getFabricId() {
		return PanoramaClient.id("panorama");
	}

	@Override
	public CompletableFuture<PanoramaShader> load(ResourceManager manager, Executor executor) {
		PanoramaClient.LOGGER.info("load");
		return CompletableFuture.supplyAsync(() -> {
			String vertex = loadStringResource(manager, PanoramaShader.VERTEX_ID);
			String fragment = loadStringResource(manager, PanoramaShader.FRAGMENT_ID);

			return new PanoramaShader(vertex, fragment, getTexture(manager));
		}, executor);
	}

	private String loadStringResource(ResourceManager manager, Identifier identifier) {
		Optional<Resource> resource = manager.getResource(identifier);

		if (resource.isPresent()) {
			try {
				return IOUtils.toString(resource.get().getInputStream(), StandardCharsets.UTF_8);
			} catch (IOException exception) {
				PanoramaClient.LOGGER.error("Filed to open input stream!", exception);
			}
		}

		return "";
	}

	private Identifier getTexture(ResourceManager manager) {
		return manager.getResource(PanoramaShader.TEXTURE_ID).isPresent() ? PanoramaShader.TEXTURE_ID : null;
	}

	@Override
	public CompletableFuture<Void> apply(PanoramaShader data, ResourceManager manager, Executor executor) {
		PanoramaClient.setShader(data);
		return CompletableFuture.completedFuture(null);
	}

}
