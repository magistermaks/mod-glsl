package net.darktree.glslmc;

import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.PanoramaResourceLoader;
import net.darktree.glslmc.render.PanoramaShader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class PanoramaClient implements ClientModInitializer {

	public static final String NAMESPACE = "glsl_panorama";
	public static final Logger LOGGER = LoggerFactory.getLogger("GLSL Panorama");
	public static final PanoramaResourceLoader LOADER = new PanoramaResourceLoader();

	private static PanoramaShader shader;
	private static PanoramaRenderer renderer;

	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(LOADER);
	}

	public static Identifier id(String name) {
		return Identifier.of(NAMESPACE, name);
	}

	public static void setShader(PanoramaShader shader) {
		PanoramaClient.shader = shader;
	}

	public static PanoramaRenderer getRenderer() {
		if (shader != null) {
			if (renderer != null) {
				renderer.close();
			}

			renderer = shader.compile();
			shader = null;
		}

		return renderer;
	}

}
