package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.*;
import org.lwjgl.opengl.GL33;

import java.util.Collections;

public class PanoramaShaderRenderer implements PanoramaRenderer {

	private final BufferBuilder buffer;
	private final int program;
	private final int timeLoc;
	private final int mouseLoc;
	private final int resolutionLoc;
	private final int alphaLoc;

	public PanoramaShaderRenderer(String vertex, String fragment) {
		int vert = compileShader(vertex, GlConst.GL_VERTEX_SHADER);
		int frag = compileShader(fragment, GlConst.GL_FRAGMENT_SHADER);

		this.buffer = Tessellator.getInstance().getBuffer();
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
		GL33.glUniform1f(timeLoc, time);
		GL33.glUniform2f(mouseLoc, mouseX, mouseY);
		GL33.glUniform2f(resolutionLoc, width, height);
		GL33.glUniform1f(alphaLoc, alpha);

		// irit, why can't we bake it? Blaze3D? more like BlazeImmediateMode
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
		buffer.vertex(-1.0f, -1.0f,  1.0f).next();
		buffer.vertex(1.0f, -1.0f,  1.0f).next();
		buffer.vertex(1.0f,  1.0f,  1.0f).next();
		buffer.vertex(-1.0f,  1.0f,  1.0f).next();
		buffer.end();

		RenderSystem.enableBlend();
		BufferRenderer.postDraw(this.buffer);
	}

	@Override
	public void close() {
		GlStateManager.glDeleteProgram(this.program);
	}

}
