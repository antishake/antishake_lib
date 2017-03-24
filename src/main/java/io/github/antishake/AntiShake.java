package io.github.antishake;

import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by ruraj on 2/19/17.
 */
public class AntiShake {

  private final static Logger logger = Logger.getLogger(AntiShake.class);

  private static Properties properties;
  static double SPRING_CONSTANT;
  private static double DAMPING_RATIO;
  private static double CIRCULAR_BUFFER_IN_SEC;
  private static double SAMPLING_RATE_IN_HZ;
  static int NO_OF_SAMPLES;
  static double SHAKE_DETECTION_THRESHOLD;
  private static double SHAKE_DETECTION_CHECK_TIME_IN_SEC;
  static int NO_OF_SAMPLES_SHAKE_DETECTION;
  private static ArrayList<Double> impulseResponseSamples;
  private static ArrayList<Coordinate> accelerometerValues;
  private static ArrayList<Coordinate> responseSamples;
  private static int earliestAccelerometerDataIndex, latestAccelerometerDataIndex;
  //private CircularBuffer circularBuffer; // To use Circular buffer once pushed

  // To load the config.properties when the class is loaded
  static {
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

  AntiShake(MotionCorrectionListener listener) {
//create CircularBuffer object
  }

  AntiShake() {

  }

  /**
   * @return true if shaking is detected in the device
   * false if there is no shaking
   */
  private boolean isShaking() {
    return isShaking(getAccelerometerValues());
  }

  /**
   * @param accelerometerValues
   * @return true if shaking is detected in the device
   * false if there is no shaking
   * <p>
   * Takes {@link AntiShake#NO_OF_SAMPLES_SHAKE_DETECTION} number of samples from the argument ArrayList accelerometerValues and
   * aggregates them. Compares the aggregated values of each axis against the {@link AntiShake#SHAKE_DETECTION_THRESHOLD}
   * to detect shaking. If the aggregated values of either of the axis is greater than the {@link AntiShake#SHAKE_DETECTION_THRESHOLD},
   * then the shaking is detected.
   */
  boolean isShaking(ArrayList<Coordinate> accelerometerValues) {
    boolean shakeDetected = false;
    if (accelerometerValues.isEmpty()) return shakeDetected;

    int latestAccelerometerDataIndex = getLatestAccelerometerDataIndex();
    int tempLatestAccelerometerDataIndex = latestAccelerometerDataIndex;
    Coordinate accelerometerValue;
    double aggregateX = 0, aggregateY = 0, aggregateZ = 0;
    for (int i = 0; i < NO_OF_SAMPLES_SHAKE_DETECTION; i++) {
      accelerometerValue = accelerometerValues.get(tempLatestAccelerometerDataIndex);
      aggregateX += accelerometerValue.getX();
      aggregateY += accelerometerValue.getY();
      aggregateZ += accelerometerValue.getZ();
      if (aggregateX > SHAKE_DETECTION_THRESHOLD || aggregateY > SHAKE_DETECTION_THRESHOLD || aggregateZ > SHAKE_DETECTION_THRESHOLD) {
        shakeDetected = true;
        break;
      }
      tempLatestAccelerometerDataIndex--;

      // If the index goes below 0, set it to the last index of the array and stop processing when it again reaches the
      // latestAccelerometerDataIndex where we started
      if (tempLatestAccelerometerDataIndex < 0) {
        tempLatestAccelerometerDataIndex = accelerometerValues.size() - 1;
      }
      if (tempLatestAccelerometerDataIndex == latestAccelerometerDataIndex) {
        break;
      }
    }
    return shakeDetected;
  }

  /**
   * Calculates the impulse response of the Spring-Mass-Damper system
   * (H(t) = t*e(-t*sqrt(k))) for {@link AntiShake#CIRCULAR_BUFFER_IN_SEC} seconds
   * with given {@link AntiShake#SAMPLING_RATE_IN_HZ}
   */
  private static void calculateImplulseResponse() {
    ArrayList<Double> impulseResponseSamples = getImpulseResponseSamples();
    double samplingRateInSeconds = (1.0d / SAMPLING_RATE_IN_HZ);
    int i = 0;
    double intervalInSeconds;
    do {
      intervalInSeconds = i * samplingRateInSeconds;
      impulseResponseSamples.add(calculateImplulseResponse(intervalInSeconds));
      i++;
    } while (intervalInSeconds < CIRCULAR_BUFFER_IN_SEC);
  }

  /**
   * Calculates impulse response of the Spring-Mass-Damper system (H(t) = t*e(-t*sqrt(k)))
   * for the given time
   *
   * @param time
   * @return impulseResponse
   */
  static double calculateImplulseResponse(final double time) {
    Double impulseResponse = time * Math.exp(-(time * Math.sqrt(SPRING_CONSTANT)));
    return impulseResponse;
  }

  /**
   * Convolves impulse response of the Spring-Mass-Damper system  (H(t) = t*e(-t*sqrt(k)))
   * with the given accelerometer input samples
   *
   * @return responseSamples
   */
  private static void calculateTransformationVector() {
    ArrayList<Double> impulseResponseSamples = getImpulseResponseSamples();
    calculateTransformationVector(impulseResponseSamples, getAccelerometerValues(), getResponseSamples());
  }

  /**
   * Convolves impulse response of the Spring-Mass-Damper system  (H(t) = t*e(-t*sqrt(k)))
   * with the given accelerometer input samples
   *
   * @param impulseResponseSamples
   * @param accelerometerValues
   * @param responseSamples
   */
  static void calculateTransformationVector(ArrayList<Double> impulseResponseSamples, ArrayList<Coordinate> accelerometerValues, ArrayList<Coordinate> responseSamples) {
    if (impulseResponseSamples == null || accelerometerValues == null) {
      return;
    }
    if (accelerometerValues.isEmpty() || impulseResponseSamples.size() != accelerometerValues.size()) {
      return;
    }

    responseSamples.clear();

    int earliestAccelerometerDataIndex = getEarliestAccelerometerDataIndex(); // get index from Geo's code
    if (earliestAccelerometerDataIndex >= NO_OF_SAMPLES) return; // index should be 0 to 200 in this testing case

    double xResponseValue, yResponseValue, zResponseValue;

    for (int i = 0; i < NO_OF_SAMPLES; i++) {
      xResponseValue = 0;
      yResponseValue = 0;
      zResponseValue = 0;

      for (int j = 0, k = i; j <= i; j++, k--) {
        xResponseValue += impulseResponseSamples.get(j) * accelerometerValues.get((earliestAccelerometerDataIndex + k) % NO_OF_SAMPLES).getX();
        yResponseValue += impulseResponseSamples.get(j) * accelerometerValues.get((earliestAccelerometerDataIndex + k) % NO_OF_SAMPLES).getY();
        zResponseValue += impulseResponseSamples.get(j) * accelerometerValues.get((earliestAccelerometerDataIndex + k) % NO_OF_SAMPLES).getZ();
      }
      responseSamples.add(new Coordinate(xResponseValue, yResponseValue, zResponseValue));
    }
  }

  /**
   * Returns the value of the given key from the config.properties file
   *
   * @param key
   * @return value
   */
  private static String getPropertyValue(String key) {
    return properties.getProperty(key);
  }

  /**
   * Loads all the properties from config.properties file and assigns to appropriate static variables
   */
  private static void loadProperties() {
    SPRING_CONSTANT = Double.parseDouble(getPropertyValue("SPRING_CONSTANT"));
    DAMPING_RATIO = Double.parseDouble(getPropertyValue("DAMPING_RATIO"));
    CIRCULAR_BUFFER_IN_SEC = Double.parseDouble(getPropertyValue("CIRCULAR_BUFFER_IN_SEC"));
    SAMPLING_RATE_IN_HZ = Double.parseDouble(getPropertyValue("SAMPLING_RATE_IN_HZ"));
    NO_OF_SAMPLES = (int) (CIRCULAR_BUFFER_IN_SEC * SAMPLING_RATE_IN_HZ) + 1; // Extra sample for the value at time 0
    SHAKE_DETECTION_THRESHOLD = Double.parseDouble(getPropertyValue("SHAKE_DETECTION_THRESHOLD"));
    SHAKE_DETECTION_CHECK_TIME_IN_SEC = Double.parseDouble(getPropertyValue("SHAKE_DETECTION_CHECK_TIME_IN_SEC"));
    NO_OF_SAMPLES_SHAKE_DETECTION = (int) (SHAKE_DETECTION_CHECK_TIME_IN_SEC * SAMPLING_RATE_IN_HZ);
  }

  /**
   * @return earliestAccelerometerDataIndex
   */
  static int getEarliestAccelerometerDataIndex() {
    earliestAccelerometerDataIndex = 0; // Get value from circular buffer
//    earliestAccelerometerDataIndex = getCircularBuffer().getReadPointer();
    return earliestAccelerometerDataIndex;
  }

  /**
   * @param earliestAccelerometerDataIndex1
   */
  static void setEarliestAccelerometerDataIndex(int earliestAccelerometerDataIndex1) {
    earliestAccelerometerDataIndex = earliestAccelerometerDataIndex1;
  }

  /**
   * @return impulseResponseSamples
   */
  static ArrayList<Double> getImpulseResponseSamples() {
    if (impulseResponseSamples == null) {
      impulseResponseSamples = new ArrayList<Double>();
    }
    return impulseResponseSamples;
  }

  /**
   * @return accelerometerValues
   */
  static ArrayList<Coordinate> getAccelerometerValues() {
    // Need to uncomment the below line once circular buffer data is pushed
    // accelerometerValues = (ArrayList<Coordinate>) Arrays.asList(getCircularBuffer().getElements());
    if (accelerometerValues == null) {
      accelerometerValues = new ArrayList<Coordinate>();
    }
    return accelerometerValues;
  }

  /**
   * @return responseSamples
   */
  static ArrayList<Coordinate> getResponseSamples() {
    if (responseSamples == null) {
      responseSamples = new ArrayList<Coordinate>(NO_OF_SAMPLES);
    }
    return responseSamples;
  }

  /**
   * @return latestAccelerometerDataIndex
   */
  public static int getLatestAccelerometerDataIndex() {
    latestAccelerometerDataIndex = 0; // Get write pointer value from circular buffer
//  latestAccelerometerDataIndex = getCircularBuffer().getWritePointer();
    return latestAccelerometerDataIndex;
  }

  /**
   * @param latestAccelerometerDataIndex1
   */
  public static void setLatestAccelerometerDataIndex(int latestAccelerometerDataIndex1) {
    latestAccelerometerDataIndex = latestAccelerometerDataIndex1;
  }

  /**
   * @return circularBuffer
   */
  /* public CircularBuffer getCircularBuffer() {
    return circularBuffer;
  }
*/
  /**
   * @param circularBuffer
   */
  /* public void setCircularBuffer(CircularBuffer circularBuffer) {
    this.circularBuffer = circularBuffer;
  }*/
}

