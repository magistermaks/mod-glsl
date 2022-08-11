package net.darktree.glslmc;

import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.PanoramaResourceLoader;
import net.darktree.glslmc.render.impl.PanoramaFallbackRenderer;
import net.darktree.glslmc.render.impl.PanoramaShaderRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class PanoramaClient implements ClientModInitializer, SimpleSynchronousResourceReloadListener {

	public static final String NAMESPACE = "glsl_panorama";
	public static final Logger LOGGER = LogManager.getLogger("GLSL Panorama");

	private static PanoramaRenderer renderer;

	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);
	}

	@Override
	public void reload(ResourceManager manager) {
		if (renderer != null) {
			renderer.close();
		}
		PanoramaResourceLoader loader = new PanoramaResourceLoader(manager);
		try {
			renderer = new PanoramaShaderRenderer(loader);
		} catch (IOException e) {
			PanoramaClient.LOGGER.error("Failed to create panorama renderer!", e);
			renderer = new PanoramaFallbackRenderer(0xEF323D, 0xFFFFFF);
		}
	}

	public static Identifier id(String name) {
		return new Identifier(NAMESPACE, name);
	}

	public static PanoramaRenderer getRenderer() {
		return renderer;
	}

	@Override
	public Identifier getFabricId() {
		return PanoramaClient.id("panorama");
	}
}
