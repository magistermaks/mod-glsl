package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL30;

import java.util.Collections;

public class PanoramaShaderRenderer implements PanoramaRenderer {

	private final VertexBuffer buffer;
	private final int program;
	private final int timeLoc;
	private final int mouseLoc;
	private final int resolutionLoc;
	private final int alphaLoc;

	public PanoramaShaderRenderer(String vertex, String fragment) {
		int vert = compileShader(vertex, GlConst.GL_VERTEX_SHADER);
		int frag = compileShader(fragment, GlConst.GL_FRAGMENT_SHADER);

		this.buffer = new VertexBuffer();;
		this.program = GlStateManager.glCreateProgram();

		GlStateManager.glAttachShader(program, vert);
		GlStateManager.glAttachShader(program, frag);
		GlStateManager.glLinkProgram(program);

		// check linking status
		if (GlStateManager.glGetProgrami(program, GlConst.GL_LINK_STATUS) == GlConst.GL_FALSE) {
			String log = GlStateManager.glGetProgramInfoLog(program, 1024);
			throw new RuntimeException("Filed to link shader program! Caused by: " + log);
		}

		// free now unused resources
		GlStateManager.glDeleteShader(vert);
		GlStateManager.glDeleteShader(frag);

		// bake buffer data
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
		builder.vertex(-1.0f, -1.0f,  1.0f).next();
		builder.vertex(1.0f, -1.0f,  1.0f).next();
		builder.vertex(1.0f,  1.0f,  1.0f).next();
		builder.vertex(-1.0f,  1.0f,  1.0f).next();
		builder.end();

		buffer.bind();
		buffer.upload(builder);
		VertexBuffer.unbind();

		// load uniforms
		this.timeLoc = GlUniform.getUniformLocation(this.program, "time");
		this.mouseLoc = GlUniform.getUniformLocation(this.program, "mouse");
		this.resolutionLoc = GlUniform.getUniformLocation(this.program, "resolution");
		this.alphaLoc = GlUniform.getUniformLocation(this.program, "alpha");
	}

	private int compileShader(String source, int type) {
		int shader = GlStateManager.glCreateShader(type);
		GlStateManager.glShaderSource(shader, Collections.singletonList(source));
		GlStateManager.glCompileShader(shader);

		// check compilation status
		if (GlStateManager.glGetShaderi(shader, GlConst.GL_COMPILE_STATUS) == GlConst.GL_FALSE) {
			String log = GlStateManager.glGetShaderInfoLog(shader, 1024);
			throw new RuntimeException("Filed to compile shader! Caused by: " + log);
		}

		return shader;
	}

	@Override
	public void draw(float time, float mouseX, float mouseY, float width, float height, float alpha) {
		GlProgramManager.useProgram(this.program);

		// update uniforms
		GL30.glUniform1f(timeLoc, time);
		GL30.glUniform2f(mouseLoc, mouseX, mouseY);
		GL30.glUniform2f(resolutionLoc, width, height);
		GL30.glUniform1f(alphaLoc, alpha);

		RenderSystem.enableBlend();
		buffer.drawVertices();
	}

	@Override
	public void close() {
		GlStateManager.glDeleteProgram(this.program);
		buffer.close();
	}

}
