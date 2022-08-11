package net.darktree.glslmc.render;

public interface PanoramaRenderer {

	void draw(float time, float mouseX, float mouseY, float width, float height, float alpha);

	void close();

}
