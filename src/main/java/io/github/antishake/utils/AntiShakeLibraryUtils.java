package io.github.antishake.utils;

import java.util.Properties;

public class AntiShakeLibraryUtils {

  /**
   * Returns the value of the given ConfigEnum key from the properties object
   * if available or default value from ConfigEnum
   *
   * @param key
   * @param properties
   * @return
   */
  public static double getPropertyValue(ConfigEnum key, Properties properties) {
    if (properties == null || !properties.containsKey(key.toString()))
      return key.getValue();
    else {
      return Double.parseDouble(properties.getProperty(key.toString()));
    }
  }
}
