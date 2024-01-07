package helper;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class Misc {


	public static boolean isNullOrEmpty(String str) {
		if (str == null) {
			return true;
		}

		return str.isEmpty();
	}

	public static boolean isNullOrWhiteSpace(String str) {
		if (str == null) {
			return true;
		}

		return str.trim().isEmpty();
	}

	public static boolean arrayContains(String[] arr, String needle) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals(needle)) {
				return true;
			}
		}

		return false;
	}

	public static boolean listContains(List<String> list, String needle) {
		String[] arr = list.toArray(new String[list.size()]);
		return arrayContains(arr, needle);
	}
	
	public static void setStdoutLogger() {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		AppenderComponentBuilder console = builder.newAppender("stdout", "Console");

		LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
		standard.addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n");
		console.add(standard);
		
		builder.add(console);

		RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);
		rootLogger.add(builder.newAppenderRef("stdout"));

		builder.add(rootLogger);
		
		Configurator.initialize(builder.build());
	}
}
