package net.darktree.glslmc.settings;

import net.minecraft.client.option.CyclingOption;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.text.TranslatableText;

public class Options {

	private static final String ENABLED_KEY = "options.glsl_panorama.enabled";
	private static final String QUALITY_KEY = "options.glsl_panorama.quality";

	public static boolean enabled = true;
	public static double quality = 1;

	static final CyclingOption<Boolean> ENABLED = CyclingOption.create(ENABLED_KEY, gameOptions -> enabled, (gameOptions, option, enable) -> {
		Options.enabled = enable;
	});

	static final DoubleOption QUALITY = new DoubleOption(QUALITY_KEY, 0.05, 1.0, 0.0f, gameOptions -> quality, (gameOptions, quality) -> {
		Options.quality = quality;
	}, (gameOptions, option) -> new TranslatableText(QUALITY_KEY, (int) (quality * 100)));

}
