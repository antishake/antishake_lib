package io.github.antishake;

import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ruraj on 2/19/17.
 */
public class AntiShake {

  private final static Logger logger = Logger.getLogger(AntiShake.class);

  private static Properties properties;
  private static String SPRING_CONSTANT;
  private static String DAMPING_RATIO;
  // To load the config.properties when the class is loaded
  static{
    InputStream is = null;
    try {
      properties = new Properties();
      is = ClassLoader.class.getResourceAsStream("/config.properties");
      properties.load(is);
      loadProperties();
    } catch (FileNotFoundException e) {
      logger.error(e);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private AntiShake(MotionCorrectionListener listener) {

  }

  /**
   * Calculates the next motion correction using accelerometer reading
   * @param x
   * @param y
   * @param z
   */
  public void calculateTranslationVector(float x, float y, float z) {

    throw new NotImplementedException();
  }


  /**
   * Returns the value of the given key from the config.properties file
   * @param key
   * @return value
   */
  private static String getPropertyValue(String key){
    return properties.getProperty(key);
  }

  /**
   * Loads all the properties from config.properties file and assigns to appropriate static variables
   */
  private static void loadProperties() {
    SPRING_CONSTANT = getPropertyValue("SPRING_CONSTANT");
    DAMPING_RATIO = getPropertyValue("DAMPING_RATIO");
  }
}
