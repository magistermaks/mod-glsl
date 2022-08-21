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

	private static final Identifier VERTEX = PanoramaClient.id("panorama/shader.vert");
	private static final Identifier FRAGMENT = PanoramaClient.id("panorama/shader.frag");
	private static final Identifier TEXTURE = PanoramaClient.id("panorama/texture.png");

	@Override
	public Identifier getFabricId() {
		return PanoramaClient.id("panorama");
	}

	@Override
	public CompletableFuture<PanoramaShader> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			String vertex = loadStringResource(manager, VERTEX);
			String fragment = loadStringResource(manager, FRAGMENT);

			return new PanoramaShader(vertex, fragment, getTexture(manager));
		}, executor);
	}

	/**
	 * Force shaders to reload even if resource reload was not triggered
	 */
	public void reload() {
		MinecraftClient client = MinecraftClient.getInstance();
		ResourceManager manager = client.getResourceManager();
		Executor executor = Util.getMainWorkerExecutor();

		load(manager, DummyProfiler.INSTANCE, executor).thenApply(shader -> apply(shader, manager, DummyProfiler.INSTANCE, executor));
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
		return manager.getResource(TEXTURE).isPresent() ? TEXTURE : null;
	}

	@Override
	public CompletableFuture<Void> apply(PanoramaShader data, ResourceManager manager, Profiler profiler, Executor executor) {
		PanoramaClient.setShader(data);
		return CompletableFuture.completedFuture(null);
	}

}
