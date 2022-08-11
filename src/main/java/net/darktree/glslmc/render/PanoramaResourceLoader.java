package net.darktree.glslmc.render;

import net.darktree.glslmc.PanoramaClient;
import net.minecraft.client.render.Shader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PanoramaResourceLoader implements ResourceFactory {
	private final String vertex;
	private final String fragment;
	private final String json;

	private static final Identifier VERTEX = PanoramaClient.id("panorama/shader.vert");
	private static final Identifier FRAGMENT = PanoramaClient.id("panorama/shader.frag");

	public PanoramaResourceLoader(ResourceManager manager) {
		// This JSON specifies what uniforms and samplers are in the shader. Preferably, this can be switched to have the shader provide this.
		this.json = """
					{
					    "blend": {
					        "func": "add",
					        "srcrgb": "srcalpha",
					        "dstrgb": "1-srcalpha"
					    },
					    "vertex": "panorama",
					    "fragment": "panorama",
					    "attributes": [
					        "Position"
					    ],
					    "uniforms": [
					        { "name": "time", "type": "float", "count": 1, "values": [ 0.0 ] },
					        { "name": "alpha", "type": "float", "count": 1, "values": [ 0.0 ] },
					        { "name": "resolution", "type": "float", "count": 2, "values": [ 0.0, 0.0 ] },
					        { "name": "mouse", "type": "float", "count": 2, "values": [ 0.0, 0.0 ] }
					    ]
					}
						""";
		this.vertex = ShaderPatcher.patch(loadStringResource(manager, VERTEX));
		this.fragment = ShaderPatcher.patch(loadStringResource(manager, FRAGMENT));

	}

	private String loadStringResource(ResourceManager manager, Identifier identifier) {
		try (Resource resource = manager.getResource(identifier)){
			return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			return "";
		}
	}

	@Override
	public Resource getResource(Identifier id) {
		final String path = id.getPath();

		if (path.endsWith("json")) {
			return new StringResource(id, json);
		} else if (path.endsWith("vsh")) {
			return new StringResource(id, vertex);
		} else if (path.endsWith("fsh")) {
			return new StringResource(id, fragment);
		}

		return null;
	}

	private static class StringResource implements Resource {
		private final Identifier id;
		private final String content;

		private StringResource(Identifier id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public Identifier getId() {
			return id;
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public boolean hasMetadata() {
			return false;
		}

		@Nullable
		@Override
		public <T> T getMetadata(ResourceMetadataReader<T> metaReader) {
			return null;
		}

		@Override
		public String getResourcePackName() {
			return "<panorama>";
		}

		@Override
		public void close() {
			// No resources to release
		}
	}
}