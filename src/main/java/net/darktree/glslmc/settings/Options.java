package net.darktree.glslmc.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.darktree.glslmc.PanoramaClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.CyclingOption;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.text.TranslatableText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class Options {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
	private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("glsl_panorama.json");
	private static final String ENABLED_KEY = "options.glsl_panorama.enabled";
	private static final String QUALITY_KEY = "options.glsl_panorama.quality";
	private static final Options INSTANCE = load();

	public boolean enabled = true;
	public double quality = 1;

	final transient CyclingOption<Boolean> ENABLED = CyclingOption.create(ENABLED_KEY, gameOptions -> enabled, (gameOptions, option, enable) -> {
		this.enabled = enable;
	});

	final transient DoubleOption QUALITY = new DoubleOption(QUALITY_KEY, 0.05, 1.0, 0.0f, gameOptions -> quality, (gameOptions, quality) -> {
		this.quality = quality;
	}, (gameOptions, option) -> new TranslatableText(QUALITY_KEY, (int) (quality * 100)));

	/**
	 * Get options instance
	 */
	public static Options get() {
		return INSTANCE;
	}

	/**
	 * Load settings from a JSON file, it there is no file it will be gendered first
	 */
	public static Options load() {
		if (!CONFIG.toFile().exists()) {
			new Options().save();
		}

		try (BufferedReader reader = Files.newBufferedReader(CONFIG)) {
			return GSON.fromJson(reader, Options.class);
		} catch (Exception exception) {
			PanoramaClient.LOGGER.error("Failed to load config!", exception);
		}

		return new Options();
	}

	/**
	 * Save settings to JSON file, if the file is missing it will be created
	 */
	public void save() {
		try {
			if (CONFIG.toFile().createNewFile()) {
				PanoramaClient.LOGGER.info("No panorama shader config found, created new one!");
			}

			try (BufferedWriter writer = Files.newBufferedWriter(CONFIG)) {
				GSON.toJson(this, writer);
			}
		} catch (Exception exception) {
			PanoramaClient.LOGGER.error("Failed to save config!", exception);
		}
	}

}
