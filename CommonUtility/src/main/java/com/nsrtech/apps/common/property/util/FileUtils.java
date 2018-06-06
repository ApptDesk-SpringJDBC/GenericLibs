package com.nsrtech.apps.common.property.util;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * 
 * @author Balaji
 * 
 */
public class FileUtils {
	private static Map<String, Properties> propsMap = new HashMap<String, Properties>();

	/**
	 * @desc Creates an InputStream Object from a given file name.
	 * @param fileName
	 * @return An Inputstream object
	 * @throws ReservationDeskException
	 *             if specified file not found
	 */
	public static InputStream getResourceAsStream(Logger logger, String fileName) throws FileNotFoundException {
		InputStream propsIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		if (propsIn == null) {
			propsIn = FileUtils.class.getResourceAsStream(fileName);
		}
		if (propsIn == null) {
			propsIn = ClassLoader.getSystemResourceAsStream(fileName);
		}

		if (propsIn == null) {
			logger.error(fileName + " not found");
			throw new FileNotFoundException("File not found!");
		}
		return propsIn;
	}

	/**
	 * @desc Creates a Properties Object from a given file name.
	 * @param fileName
	 * @return A Properties object
	 * @throws ReservationDeskException
	 */
	public static Properties getProperties(Logger logger, String fileName) throws Exception {
		Properties properties = propsMap.get(fileName);
		if (properties != null) {
			return properties;
		}

		try {
			properties = new Properties();
			properties.load(getResourceAsStream(logger, fileName));
			propsMap.put(fileName, properties);
		} catch (IOException e) {
			logger.error("Property read error");
			throw new FileNotFoundException("File not found!");
		}

		return properties;
	}

	/**
	 * @desc Refreshes a Properties Object from a given file name by assigning
	 *       it to null.
	 * @param fileName
	 * @return A Properties object
	 * @throws ReservationDeskException
	 */
	public static Properties refreshProperties(Logger logger, String fileName) throws Exception {
		Properties properties = new Properties();
		try {
			properties.load(getResourceAsStream(logger, fileName));
			propsMap.put(fileName, null);
		} catch (IOException e) {
			logger.error("Error in refresh properties:");
			throw new FileNotFoundException("File not found!");
		}
		return properties;
	}

	public static String getValueFromProperties(Logger logger, String key, String fileName) throws Exception {
		Properties properties = getProperties(logger, fileName);
		return (String) properties.get(key);
	}
}
