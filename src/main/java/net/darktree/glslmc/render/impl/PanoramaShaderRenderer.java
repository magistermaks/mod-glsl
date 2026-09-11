package net.darktree.glslmc.render.impl;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.darktree.glslmc.render.GlobalState;
import net.darktree.glslmc.render.PanoramaRenderer;
import net.darktree.glslmc.render.ScalableCanvas;
import net.darktree.glslmc.settings.Options;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;

public final class PanoramaShaderRenderer implements PanoramaRenderer {

	private final GpuBuffer buffer;
	private final Identifier texture;
	private final int program;

	private final int timeLoc;
	private final int mouseLoc;
	private final int resolutionLoc;
	private final int imageLoc;
	private final int backbufferLoc;
	private final int frameLoc;
	private final int persistentFrameLoc;
	private final int speedLoc;

	private final ScalableCanvas canvas;
	private final TextureManager manager;

	public PanoramaShaderRenderer(String vertex, String fragment, Identifier texture) {
		int vert = compileShader(vertex, GlConst.GL_VERTEX_SHADER);
		int frag = compileShader(fragment, GlConst.GL_FRAGMENT_SHADER);

		this.canvas = new ScalableCanvas();
		this.manager = MinecraftClient.getInstance().getTextureManager();

		this.program = GlStateManager.glCreateProgram();
		this.texture = texture;

		GlStateManager.glAttachShader(program, vert);
		GlStateManager.glAttachShader(program, frag);
		GlStateManager.glLinkProgram(program);

		// check linking status
		if (GlStateManager.glGetProgrami(program, GlConst.GL_LINK_STATUS) == GlConst.GL_FALSE) {
			String log = GlStateManager.glGetProgramInfoLog(program, 1024);
			throw new RuntimeException("Failed to link shader program! Caused by: " + log);
		}

		// free now unused resources
		GlStateManager.glDeleteShader(vert);
		GlStateManager.glDeleteShader(frag);

		// bake buffer data
		BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		builder.vertex(-1.0f, -1.0f,  1.0f).texture(0, 0).color(1f, 1f, 1f, 1f);
		builder.vertex( 1.0f, -1.0f,  1.0f).texture(1, 0).color(1f, 1f, 1f, 1f);
		builder.vertex( 1.0f,  1.0f,  1.0f).texture(1, 1).color(1f, 1f, 1f, 1f);
		builder.vertex(-1.0f,  1.0f,  1.0f).texture(0, 1).color(1f, 1f, 1f, 1f);

		this.buffer = RenderSystem.getDevice().createBuffer(() -> "panorama surface", BufferType.VERTICES, BufferUsage.STATIC_WRITE, builder.end().getBuffer());

		// load uniforms
		this.timeLoc = GlUniform.getUniformLocation(this.program, "time");
		this.mouseLoc = GlUniform.getUniformLocation(this.program, "mouse");
		this.resolutionLoc = GlUniform.getUniformLocation(this.program, "resolution");
		this.imageLoc = GlUniform.getUniformLocation(this.program, "image");
		this.backbufferLoc = GlUniform.getUniformLocation(this.program, "backbuffer");
		this.frameLoc = GlUniform.getUniformLocation(this.program, "frame");
		this.persistentFrameLoc = GlUniform.getUniformLocation(this.program, "persistent_frame");
		this.speedLoc = GlUniform.getUniformLocation(this.program, "speed");
	}

	private int compileShader(String source, int type) {
		int shader = GlStateManager.glCreateShader(type);
		GlStateManager.glShaderSource(shader, source);
		GlStateManager.glCompileShader(shader);

		// check compilation status
		if (GlStateManager.glGetShaderi(shader, GlConst.GL_COMPILE_STATUS) == GlConst.GL_FALSE) {
			String log = GlStateManager.glGetShaderInfoLog(shader, 1024);
			throw new RuntimeException("Filed to compile shader! Caused by: " + log);
		}

		return shader;
	}

	@Override
	public void draw(MinecraftClient client, double time, long frame, float mouseX, float mouseY, int width, int height, float alpha) {
		GlStateManager._glUseProgram(this.program);

		float scale = (float) Options.get().quality;
		canvas.resize((int) (width * scale), (int) (height * scale));
		// TODO begin canvas write

		// bind sampler if present
		if (texture != null) {
			GlTexture tex = (GlTexture) manager.getTexture(texture).getGlTexture();
			GlStateManager._activeTexture(GlConst.GL_TEXTURE0);
			GlStateManager._bindTexture(tex.getGlId());
			GlStateManager._glUniform1i(imageLoc, 0);
		}

		// update uniforms
		GL30.glUniform1f(timeLoc, (float) time);
		GL30.glUniform2f(mouseLoc, mouseX, mouseY);
		GL30.glUniform2f(resolutionLoc, canvas.width(), canvas.height());
		GL30.glUniform1f(speedLoc, client.options.getPanoramaSpeed().getValue().floatValue());

		GlStateManager._glUniform1i(frameLoc, (int) frame);
		GlStateManager._glUniform1i(persistentFrameLoc, GlobalState.getFrame());

		// backbuffer
		GlStateManager._activeTexture(GlConst.GL_TEXTURE1);



		GlStateManager._glUniform1i(backbufferLoc, 1);

		// TODO draw
		canvas.blit(alpha);
	}

	@Override
	public void close() {
		GlStateManager.glDeleteProgram(this.program);
		buffer.close();
		canvas.close();
	}

}
