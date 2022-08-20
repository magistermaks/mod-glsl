package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL30;

import java.util.Collections;

public class PanoramaShaderRenderer implements PanoramaRenderer {

	private final VertexBuffer buffer;
	private final Identifier texture;
	private final int program;
	private final int timeLoc;
	private final int mouseLoc;
	private final int resolutionLoc;
	private final int imageLoc;

	private final Framebuffer shaderFramebuffer;
	private final Framebuffer mainFramebuffer;

	public PanoramaShaderRenderer(String vertex, String fragment, Identifier texture) {
		int vert = compileShader(vertex, GlConst.GL_VERTEX_SHADER);
		int frag = compileShader(fragment, GlConst.GL_FRAGMENT_SHADER);

		this.mainFramebuffer = MinecraftClient.getInstance().getFramebuffer();
		this.shaderFramebuffer = new SimpleFramebuffer(mainFramebuffer.textureWidth, mainFramebuffer.textureHeight, false, false);

		this.buffer = new VertexBuffer();
		this.program = GlStateManager.glCreateProgram();
		this.texture = texture;

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
		builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		builder.vertex(-1.0f, -1.0f,  1.0f).texture(0, 0).color(1f, 1f, 1f, 1f).next();
		builder.vertex( 1.0f, -1.0f,  1.0f).texture(1, 0).color(1f, 1f, 1f, 1f).next();
		builder.vertex( 1.0f,  1.0f,  1.0f).texture(1, 1).color(1f, 1f, 1f, 1f).next();
		builder.vertex(-1.0f,  1.0f,  1.0f).texture(0, 1).color(1f, 1f, 1f, 1f).next();
		builder.end();

		buffer.upload(builder);

		// load uniforms
		this.timeLoc = GlUniform.getUniformLocation(this.program, "time");
		this.mouseLoc = GlUniform.getUniformLocation(this.program, "mouse");
		this.resolutionLoc = GlUniform.getUniformLocation(this.program, "resolution");
		this.imageLoc = GlUniform.getUniformLocation(this.program, "image");
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

		shaderFramebuffer.beginWrite(false);

		// bind sampler is present
		if (texture != null) {
			MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
			RenderSystem.enableTexture();

			GL30.glUniform1i(imageLoc, GlStateManager._getActiveTexture());
		}

		// update uniforms
		GL30.glUniform1f(timeLoc, time);
		GL30.glUniform2f(mouseLoc, mouseX, mouseY);
		GL30.glUniform2f(resolutionLoc, shaderFramebuffer.textureWidth, shaderFramebuffer.textureHeight);

		// draw
		buffer.drawVertices();

		// copy into the main framebuffer
		mainFramebuffer.beginWrite(true);

		RenderSystem.setShaderTexture(0, shaderFramebuffer.getColorAttachment());
		RenderSystem.setShaderColor(1, 1, 1, alpha);

		Matrix4f matrix = new Matrix4f();
		matrix.loadIdentity();

		RenderSystem.enableBlend();
		buffer.setShader(matrix, matrix, GameRenderer.getPositionTexColorShader());
	}

	@Override
	public void close() {
		GlStateManager.glDeleteProgram(this.program);
		buffer.close();
	}

}
