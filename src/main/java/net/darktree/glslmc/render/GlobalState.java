package net.darktree.glslmc.render;

public class GlobalState {

	private static boolean swapped = true;
	private static int frame = 0;

	public static void swapFrame() {
		swapped = true;
	}

	public static void nextFrame() {
		if (swapped) {
			frame ++;
		}

		swapped = false;
	}

	public static int getFrame() {
		return frame;
	}

}
