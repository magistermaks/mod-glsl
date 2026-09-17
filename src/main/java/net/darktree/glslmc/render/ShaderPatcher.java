package net.darktree.glslmc.render;

import net.darktree.glslmc.PanoramaClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ShaderPatcher {

	private static final String FRAGMENT_OLD = "gl_FragColor";
	private static final String FRAGMENT_NEW = "fragmentColor";
	private static final Pattern UNIFORM_REGEX = Pattern.compile("^[^\\S\\r\\n]*uniform\\s+[\\w\\s]+;$", Pattern.MULTILINE);

	private static final String PREAMBLE = """
			#version 330
			
			layout(std140) uniform info {
			    uniform float time;
			    uniform vec2 mouse;
			    uniform vec2 resolution;
			    uniform int frame;
			    uniform int persistent_frame;
			    uniform float speed;
			    uniform float mouse_left_pressed;
			    uniform float mouse_right_pressed;
			};
			
			uniform sampler2D image;
			uniform sampler2D backbuffer;
			
			#line 1 0
			""";

	private static String patchFragmentOutput(String shader) {
		if (shader.contains(FRAGMENT_OLD)) {
			shader = "out vec4 " + FRAGMENT_NEW + ";\n" + shader.replace(FRAGMENT_OLD, FRAGMENT_NEW);
			PanoramaClient.LOGGER.warn("Loaded Panorama Shader uses outdated OpenGL keyword '{}', consider switching to explicit fragment shader output (out vec4)!", FRAGMENT_OLD);
		}

		return shader;
	}

	private static String patchUniformDeclarations(String shader) {
		Matcher matcher = UNIFORM_REGEX.matcher(shader);
		StringBuilder builder = new StringBuilder();
		boolean usesUniforms = false;

		while (matcher.find()) {
			matcher.appendReplacement(builder, "/* legacy uniform */");
			usesUniforms = true;
		}

		if (usesUniforms) {
			PanoramaClient.LOGGER.warn("Loaded Panorama Shader uses outdated uniform statements, uniform declarations are now redundant!");
		}

		matcher.appendTail(builder);
		return builder.toString();
	}

	private static String patchPreamble(String shader) {
		return PREAMBLE + shader;
	}

	public static String patch(String shader) {
		return Stream.of(shader)
				.map(ShaderPatcher::patchFragmentOutput)
				.map(ShaderPatcher::patchUniformDeclarations)
				.map(ShaderPatcher::patchPreamble)
				.findFirst().orElseThrow();
	}

}
