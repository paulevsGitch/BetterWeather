package paulevs.betterweather.config;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class represents config entry
 * @param <T> stored data type
 */
public class ConfigEntry <T> {
	private final List<String> comments = new ArrayList<>();
	private final String name;
	protected final T value;
	
	protected ConfigEntry(String name, T value, List<String> comments) {
		this.comments.addAll(comments);
		this.value = value;
		this.name = name;
	}
	
	protected void append(FileWriter writer) throws IOException {
		for (String comment : comments) {
			writer.append("# ");
			writer.append(comment);
			writer.append('\n');
		}
		writer.append(name);
		writer.append(" = ");
		writer.append(value.toString());
		writer.append('\n');
	}
}
