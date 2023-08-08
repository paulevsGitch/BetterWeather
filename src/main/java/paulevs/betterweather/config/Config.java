package paulevs.betterweather.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Config holder class, can save/load config data and provide stored values.
 * Values will be updated if necessary when "addEntry" method is called
 */
public class Config {
	private final Map<String, ConfigEntry<?>> entries = new HashMap<>();
	private final Map<String, String> preEntries = new HashMap<>();
	private final List<String> order = new ArrayList<>();
	private final File file;
	
	public Config(File file) {
		this.file = file;
		if (file.exists()) load();
	}
	
	/**
	 * Saves config file if necessary (there are changes that require saving)
	 */
	public void save() {
		if (!file.exists()) writeFile();
		else if (entries.size() != preEntries.size()) writeFile();
	}
	
	/**
	 * Add boolean entry to the config.
	 * If there is stored value in File it will use it instead
	 * @param name {@link String} entry name
	 * @param value {@code boolean} value
	 * @param comments Array of strings, comments will be added before entry. It is recommended to mention default value
	 */
	public void addEntry(String name, boolean value, String... comments) {
		String stored = preEntries.get(name);
		if (stored != null) value = Boolean.parseBoolean(stored);
		entries.put(name, new ConfigEntry<>(name, value, List.of(comments)));
		order.add(name);
	}
	
	/**
	 * Add float entry to the config.
	 * If there is stored value in File it will use it instead
	 * @param name {@link String} entry name
	 * @param value {@code float} value
	 * @param comments Array of strings, comments will be added before entry. It is recommended to mention default value
	 */
	public void addEntry(String name, float value, String... comments) {
		String stored = preEntries.get(name);
		if (stored != null) value = Float.parseFloat(stored);
		entries.put(name, new ConfigEntry<>(name, value, List.of(comments)));
		order.add(name);
	}
	
	/**
	 * Add int entry to the config.
	 * If there is stored value in File it will use it instead
	 * @param name {@link String} entry name
	 * @param value {@code int} value
	 * @param comments Array of strings, comments will be added before entry. It is recommended to mention default value
	 */
	public void addEntry(String name, int value, String... comments) {
		String stored = preEntries.get(name);
		if (stored != null) value = Integer.parseInt(stored);
		entries.put(name, new ConfigEntry<>(name, value, List.of(comments)));
		order.add(name);
	}
	
	/**
	 * Add {@link String} entry to the config.
	 * If there is stored value in File it will use it instead
	 * @param name {@link String} entry name
	 * @param value {@link String} value
	 * @param comments Array of strings, comments will be added before entry. It is recommended to mention default value
	 */
	public void addEntry(String name, String value, String... comments) {
		String stored = preEntries.get(name);
		if (stored != null) value = stored;
		entries.put(name, new ConfigEntry<>(name, value, List.of(comments)));
		order.add(name);
	}
	
	/**
	 * Get boolean value from the config
	 * @param name {@link String} entry name
	 * @return {@code boolean}
	 */
	public boolean getBool(String name) {
		return (Boolean) entries.get(name).value;
	}
	
	/**
	 * Get float value from the config
	 * @param name {@link String} entry name
	 * @return {@code float}
	 */
	public float getFloat(String name) {
		return (Float) entries.get(name).value;
	}
	
	/**
	 * Get int value from the config
	 * @param name {@link String} entry name
	 * @return {@code int}
	 */
	public int getInt(String name) {
		return (Integer) entries.get(name).value;
	}
	
	/**
	 * Get {@link String} value from the config
	 * @param name {@link String} entry name
	 * @return {@link String}
	 */
	public String getString(String name) {
		return (String) entries.get(name).value;
	}
	
	private void writeFile() {
		file.getParentFile().mkdirs();
		int max = entries.size() - 1;
		try {
			FileWriter writer = new FileWriter(file);
			for (int i = 0; i < order.size(); i++) {
				entries.get(order.get(i)).append(writer);
				if (i < max) writer.append('\n');
			}
			writer.flush();
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void load() {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(file.toPath());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		if (lines == null) return;
		lines.stream().filter(line -> line.length() > 2 && line.charAt(0) != '#').forEach(line -> {
			int split = line.indexOf('=');
			String name = line.substring(0, split).trim();
			String value = line.substring(split + 1).trim();
			preEntries.put(name, value);
		});
	}
}
