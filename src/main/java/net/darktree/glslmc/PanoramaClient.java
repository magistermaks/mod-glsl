package net.darktree.glslmc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class PanoramaClient {

	public static final String NAMESPACE = "glsl_panorama";
	public static final Logger LOGGER = LoggerFactory.getLogger("GLSL Panorama");

	public static Identifier id(String name) {
		return Identifier.fromNamespaceAndPath(NAMESPACE, name);
	}

}
