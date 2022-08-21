package net.darktree.glslmc.render;

public interface PanoramaRenderer {

	void draw(float time, float mouseX, float mouseY, int width, int height, float alpha);

	void close();

}
