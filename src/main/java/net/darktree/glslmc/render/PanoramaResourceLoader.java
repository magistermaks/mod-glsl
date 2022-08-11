package net.darktree.glslmc.render;

import net.darktree.glslmc.PanoramaClient;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PanoramaResourceLoader implements SimpleResourceReloadListener<PanoramaShader> {

	private static final Identifier VERTEX = PanoramaClient.id("panorama/shader.vert");
	private static final Identifier FRAGMENT = PanoramaClient.id("panorama/shader.frag");

	@Override
	public Identifier getFabricId() {
		return PanoramaClient.id("panorama");
	}

	@Override
	public CompletableFuture<PanoramaShader> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			String vertex = loadStringResource(manager, VERTEX);
			String fragment = loadStringResource(manager, FRAGMENT);

			return new PanoramaShader(vertex, fragment);
		}, executor);
	}

	private String loadStringResource(ResourceManager manager, Identifier identifier) {
		try (Resource resource = manager.getResource(identifier)){
			return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			return "";
		}
	}

	@Override
	public CompletableFuture<Void> apply(PanoramaShader data, ResourceManager manager, Profiler profiler, Executor executor) {
		PanoramaClient.setShader(data);
		return CompletableFuture.completedFuture(null);
	}

}
