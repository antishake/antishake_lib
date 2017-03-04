package io.github.antishake;

import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by ruraj on 2/19/17.
 */
public class AntiShake {

  private final static Logger logger = Logger.getLogger(AntiShake.class);

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
}
