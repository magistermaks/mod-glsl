package net.darktree.glslmc;

import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.PanoramaResourceLoader;
import net.darktree.glslmc.render.PanoramaShader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class PanoramaClient implements ClientModInitializer {

	public static final String NAMESPACE = "glsl_panorama";
	public static final Logger LOGGER = LogManager.getLogger("GLSL Panorama");
	private static final PanoramaResourceLoader PANORAMA = new PanoramaResourceLoader();
	public static final KeyBinding RELOAD_KEYBIND =  new KeyBinding("key.glsl_panorama.shader_reload", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.glsl_panorama.keys");

	private static PanoramaShader shader;
	private static PanoramaRenderer renderer;

	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(PANORAMA);
		KeyBindingHelper.registerKeyBinding(RELOAD_KEYBIND);
	}

	public static Identifier id(String name) {
		return new Identifier(NAMESPACE, name);
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
