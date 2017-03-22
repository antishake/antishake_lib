package io.github.antishake;

import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
  private static ArrayList<Double> impulseResponseSamples;
  private static ArrayList<Coordinate> accelerometerValues;
  private static ArrayList<Coordinate> responseSamples;
  private static int earliestAccelerometerDataIndex;

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

  }

  AntiShake() {

  }
    /**
     * Checks whether the device is shaking or not
     * by comparing the sum of the values from accelerometer by the empirically determined threshold
     *
     **/

    public void isShaking() {

        {
            {
                int i;
               //For now defining threshold with a dummy value that is 0.5
                double threshold = 0.5 ;

                double[] inputArray = new double[200];
                for (i = 0; i < inputArray.length; i++) {
                    inputArray[i] = i;
                }
                //For now taking the dummy value for read pointer until the circular buffer is ready to implement
                int readPointer = 5;
                double newValue = 0;
                readPointer = i;
                int count = 0;
                for (i = readPointer; i < inputArray.length; i++) {

                    while (count < 90) {

                        if (readPointer < 0) {
                            readPointer = 199;
                        }
                        newValue = newValue + inputArray[readPointer] ;

                        readPointer--;
                        count++;
                        inputArray[i] = newValue;

                        if (newValue > threshold) {
                            System.out.println("Shake Detected");
                        } else {
                            System.out.println("NoShake detected");

                        }
                    }
                }
            }
        }
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
    if (impulseResponseSamples.size() != accelerometerValues.size()) {
      return;
    }
    if (responseSamples != null) {
      responseSamples.clear();
    }
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
  }

  /**
   * @return earliestAccelerometerDataIndex
   */
  static int getEarliestAccelerometerDataIndex() {
    earliestAccelerometerDataIndex = 0; // Get value from circular buffer
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
    if (accelerometerValues == null) {
      accelerometerValues = new ArrayList<Coordinate>(NO_OF_SAMPLES);
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
}

