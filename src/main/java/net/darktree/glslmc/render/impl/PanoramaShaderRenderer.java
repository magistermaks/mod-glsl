package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.PanoramaResourceLoader;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class PanoramaShaderRenderer implements PanoramaRenderer {

	private VertexBuffer buffer;
	private final Shader program;
	private final Uniform timeLoc;
	private final Uniform mouseLoc;
	private final Uniform resolutionLoc;
	private final Uniform alphaLoc;

	public PanoramaShaderRenderer(PanoramaResourceLoader loader) throws IOException {
		buffer = new VertexBuffer();

		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		// irit, why can't we bake it? Blaze3D? more like BlazeImmediateMode
		builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
		builder.vertex(-1.0f, -1.0f,  1.0f).next();
		builder.vertex(1.0f, -1.0f,  1.0f).next();
		builder.vertex(1.0f,  1.0f,  1.0f).next();
		builder.vertex(-1.0f,  1.0f,  1.0f).next();
		builder.end();

		buffer.bind();
		buffer.upload(builder);
		VertexBuffer.unbind();

		this.program = new Shader(loader, "panorama", VertexFormats.POSITION);


		// load uniforms
		this.timeLoc = program.getUniformOrDefault("time");
		this.mouseLoc = program.getUniformOrDefault("mouse");
		this.resolutionLoc = program.getUniformOrDefault("resolution");
		this.alphaLoc = program.getUniformOrDefault("alpha");
	}

	@Override
	public void draw(float time, float mouseX, float mouseY, float width, float height, float alpha) {
		program.bind();

		// update uniforms
		timeLoc.set(time);
		mouseLoc.set(mouseX, mouseY);
		resolutionLoc.set(width, height);
		alphaLoc.set(alpha);

		RenderSystem.enableBlend();

		buffer.drawVertices();
	}

	@Override
	public void close() {
		this.buffer.close();
		this.program.close();
	}

}
