package io.github.antishake.utils;

/**
 * Created by Saranya Shanmugam on 4/5/2017.
 * <p>
 * Provides constants that will be used as keys to access values from properties file
 */
public enum ConfigEnum {

  SPRING_CONSTANT(10),
  CIRCULAR_BUFFER_IN_SEC(4),  // Number of seconds that the circular buffer should have the latest data
  SAMPLING_RATE_IN_HZ(50),  // Sampling rate to get the accelerometer data
  SHAKE_DETECTION_THRESHOLD(46),  // Threshold to compare for shake detection
  SHAKE_DETECTION_CHECK_TIME_IN_SEC(1.8),  // To check the shake detection, number of seconds that the latest data should be considered from circular buffer
  TUNE_CONVOLVE_OUTPUT(2);  // To tune the convolution output

  private final double value;

  ConfigEnum(double value) {
    this.value = value;
  }

  public double getValue() {
    return value;
  }
}
