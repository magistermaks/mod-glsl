package net.darktree.glslmc.render;

import net.darktree.glslmc.PanoramaClient;

public class ShaderPatcher {

	private static final String FRAGMENT_OLD = "gl_FragColor";
	private static final String FRAGMENT_NEW = "fragmentColor";

	public static String patch(String shader) {
		if (shader.contains(FRAGMENT_OLD)) {
			shader = "out vec4 " + FRAGMENT_NEW + ";\n" + shader.replace(FRAGMENT_OLD, FRAGMENT_NEW);
			PanoramaClient.LOGGER.warn("Loaded shader uses outdated OpenGL keyword 'gl_FragColor'!");
		}

		return "#version 330\n" + shader;
	}

}
