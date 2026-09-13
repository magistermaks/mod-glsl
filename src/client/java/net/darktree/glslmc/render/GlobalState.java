package net.darktree.glslmc.render;

public class GlobalState {

	private static int frame = 0;

	public static void nextFrame() {
		frame ++;
	}

	public static int getFrame() {
		return frame;
	}

}
