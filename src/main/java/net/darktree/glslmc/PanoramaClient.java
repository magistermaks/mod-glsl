package net.darktree.glslmc;

import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.PanoramaResourceLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class PanoramaClient implements ClientModInitializer {

	public static final String NAMESPACE = "glslmc";
	public static final Logger LOGGER = LogManager.getLogger("GLSL Panorama");
	private static final PanoramaResourceLoader PANORAMA = new PanoramaResourceLoader();

	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(PANORAMA);
	}

	public static Identifier id(String name) {
		return new Identifier(NAMESPACE, name);
	}

	public static PanoramaRenderer getRenderer() {
		return PANORAMA.getSource().getRenderer();
	}

}
