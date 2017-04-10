package io.github.antishake;

import java.util.ArrayList;
import java.util.Properties;

import io.github.antishake.utils.AntiShakeLibraryUtils;
import io.github.antishake.utils.ConfigEnum;

/**
 * Created by ruraj on 2/19/17.
 */
public class AntiShake {

//  private final static Logger logger = Logger.getLogger(AntiShake.class);

   double SPRING_CONSTANT;
  // Number of seconds that the circular buffer should have the latest data
  private double CIRCULAR_BUFFER_IN_SEC;
  // Sampling rate to get the accelerometer data
  private double SAMPLING_RATE_IN_HZ;
  // Calculated based on CIRCULAR_BUFFER_IN_SEC and SAMPLING_RATE_IN_HZ
  static int NO_OF_SAMPLES;
  // Threshold to compare for shake detection
  double SHAKE_DETECTION_THRESHOLD;
  // To check the shake detection, number of seconds that the latest data should be considered from circular buffer
  private double SHAKE_DETECTION_CHECK_TIME_IN_SEC;
  // Calculated based on SHAKE_DETECTION_CHECK_TIME_IN_SEC * SAMPLING_RATE_IN_HZ
  private static int NO_OF_SAMPLES_SHAKE_DETECTION;
  // To tune the convolution output.
  double TUNE_CONVOLVE_OUTPUT;

  private boolean wasShaking;
  private Properties properties;

  private ArrayList<Double> impulseResponseSamples;
  private ArrayList<Coordinate> accelerometerValues;
  private ArrayList<Coordinate> responseSamples, tunedResponseSamples;
  private int earliestAccelerometerDataIndex, latestAccelerometerDataIndex;
  private CircularBuffer circularBuffer;
  private MotionCorrectionListener motionCorrectionListener;

  public AntiShake(MotionCorrectionListener listener, Properties properties) {
    this.properties = properties;
    this.motionCorrectionListener = listener;
    loadProperties();

    // As impulse response of Spring-Mass-Damper system is constant for the given SPRING_CONSTANT, we calculate only once while AntiShake object creation.
    calculateImplulseResponse();
  }

  // This constructor is not exposed with public access
  AntiShake() {
    loadProperties();
  }

  /**
   * Gets called by the client application with accelerometer values
   * to calculate transformation vector
   * TODO Make this function use Coordinate instead
   *
   * @param xAxisValue
   * @param yAxisValue
   * @param zAxisValue
   */
  public void calculateTransformationVector(double xAxisValue, double yAxisValue, double zAxisValue) {
    getCircularBuffer().add(new Coordinate(xAxisValue, yAxisValue, zAxisValue));
    calculateTransformationVector();
  }

  /**
   * Checks if there is any shaking detected. If yes, calculates transformation vector and tunes it
   * and also gives the response back to the client application through listener.
   * If no, no response is given back to client application
   */
  private void calculateTransformationVector() {
    if (isShaking()) {
      wasShaking = true;
      convolve();
      tune();
      //send data through motionCorrectionListener
      motionCorrectionListener.onTranslationVectorReceived(getTunedResponseSamples());
    } else if (wasShaking) {
      wasShaking = false;
      motionCorrectionListener.onDeviceSteady();
    }
  }

  /**
   * Tunes the convolved samples using a empirically determined
   * {@link AntiShake#TUNE_CONVOLVE_OUTPUT} value
   */
  private void tune() {
    tune(getResponseSamples());
  }

  /**
   * Tunes the given convolved response samples using a empirically determined
   * {@link AntiShake#TUNE_CONVOLVE_OUTPUT} value
   *
   * @param convolvedResponseSamples
   */
  void tune(ArrayList<Coordinate> convolvedResponseSamples) {
    if (convolvedResponseSamples == null || convolvedResponseSamples.isEmpty())
      return;

    getTunedResponseSamples().clear();
    double tunedResponseSampleX, tunedResponseSampleY, tunedResponseSampleZ;
    for (Coordinate coordinate : convolvedResponseSamples) {
      tunedResponseSampleX = coordinate.getX() * TUNE_CONVOLVE_OUTPUT;
      tunedResponseSampleY = coordinate.getY() * TUNE_CONVOLVE_OUTPUT;
      tunedResponseSampleZ = coordinate.getZ() * TUNE_CONVOLVE_OUTPUT;

      getTunedResponseSamples().add(new Coordinate(tunedResponseSampleX, tunedResponseSampleY, tunedResponseSampleZ));
    }
  }

  /**
   * @return true if shaking is detected in the device false if there is no
   * shaking
   */
  private boolean isShaking() {
    return isShaking(getAccelerometerValues());
  }

  /**
   * @param accelerometerValues
   * @return true if shaking is detected in the device false if there is no
   * shaking
   * <p>
   * Takes {@link AntiShake#NO_OF_SAMPLES_SHAKE_DETECTION} number of
   * samples from the argument ArrayList accelerometerValues and
   * aggregates them. Compares the aggregated values of each axis
   * against the {@link AntiShake#SHAKE_DETECTION_THRESHOLD} to detect
   * shaking. If the aggregated values of either of the axis is
   * greater than the {@link AntiShake#SHAKE_DETECTION_THRESHOLD},
   * then the shaking is detected.
   */
  boolean isShaking(ArrayList<Coordinate> accelerometerValues) {
    boolean shakeDetected = false;
    if (accelerometerValues == null || accelerometerValues.isEmpty())
      return shakeDetected;

    int latestAccelerometerDataIndex = getLatestAccelerometerDataIndex();
    int tempLatestAccelerometerDataIndex = latestAccelerometerDataIndex;
    Coordinate accelerometerValue;
    double aggregateX = 0, aggregateY = 0, aggregateZ = 0;
    for (int i = 0; i < NO_OF_SAMPLES_SHAKE_DETECTION; i++) {
      accelerometerValue = accelerometerValues.get(tempLatestAccelerometerDataIndex);
      aggregateX += Math.abs(accelerometerValue.getX());
      aggregateY += Math.abs(accelerometerValue.getY());
      aggregateZ += Math.abs(accelerometerValue.getZ());
      if (aggregateX > SHAKE_DETECTION_THRESHOLD || aggregateY > SHAKE_DETECTION_THRESHOLD
        || aggregateZ > SHAKE_DETECTION_THRESHOLD) {
        shakeDetected = true;
        break;
      }
      tempLatestAccelerometerDataIndex--;

      // If the index goes below 0, set it to the last index of the array
      // and stop processing when it again reaches the
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
   * Calculates the impulse response of the Spring-Mass-Damper system (H(t) =
   * t*e(-t*sqrt(k))) for {@link AntiShake#CIRCULAR_BUFFER_IN_SEC} seconds
   * with given {@link AntiShake#SAMPLING_RATE_IN_HZ}
   */
  private void calculateImplulseResponse() {
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
   * Calculates impulse response of the Spring-Mass-Damper system (H(t) =
   * t*e(-t*sqrt(k))) for the given time
   *
   * @param time
   * @return impulseResponse
   */
  double calculateImplulseResponse(final double time) {
    Double impulseResponse = time * Math.exp(-(time * Math.sqrt(SPRING_CONSTANT)));
    return impulseResponse;
  }

  /**
   * Convolves impulse response of the Spring-Mass-Damper system (H(t) =
   * t*e(-t*sqrt(k))) with the given accelerometer input samples
   *
   * @return responseSamples
   */
  private void convolve() {
    ArrayList<Double> impulseResponseSamples = getImpulseResponseSamples();
    convolve(impulseResponseSamples, getAccelerometerValues(), getResponseSamples());
  }

  /**
   * Convolves impulse response of the Spring-Mass-Damper system (H(t) =
   * t*e(-t*sqrt(k))) with the given accelerometer input samples
   *
   * @param impulseResponseSamples
   * @param accelerometerValues
   * @param responseSamples
   */
  void convolve(ArrayList<Double> impulseResponseSamples, ArrayList<Coordinate> accelerometerValues, ArrayList<Coordinate> responseSamples) {
    if (impulseResponseSamples == null || accelerometerValues == null) {
      return;
    }
    if (accelerometerValues.isEmpty() /*|| impulseResponseSamples.size() != accelerometerValues.size()*/) {
      return;
    }

    responseSamples.clear();

    int earliestAccelerometerDataIndex = getEarliestAccelerometerDataIndex();

    if (earliestAccelerometerDataIndex >= NO_OF_SAMPLES)
      return; // index should be 0 to 200 in this testing case

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
   * Loads all the properties from config.properties file and assigns to
   * appropriate static variables
   */
  private void loadProperties() {
    SPRING_CONSTANT = AntiShakeLibraryUtils.getPropertyValue(ConfigEnum.SPRING_CONSTANT, properties);
    CIRCULAR_BUFFER_IN_SEC = AntiShakeLibraryUtils.getPropertyValue(ConfigEnum.CIRCULAR_BUFFER_IN_SEC, properties);
    SAMPLING_RATE_IN_HZ = AntiShakeLibraryUtils.getPropertyValue(ConfigEnum.SAMPLING_RATE_IN_HZ, properties);
    NO_OF_SAMPLES = (int) (CIRCULAR_BUFFER_IN_SEC * SAMPLING_RATE_IN_HZ) + 1; // Extra sample for the value at time 0
    SHAKE_DETECTION_THRESHOLD = AntiShakeLibraryUtils.getPropertyValue(ConfigEnum.SHAKE_DETECTION_THRESHOLD, properties);
    SHAKE_DETECTION_CHECK_TIME_IN_SEC = AntiShakeLibraryUtils.getPropertyValue(ConfigEnum.SHAKE_DETECTION_CHECK_TIME_IN_SEC, properties);
    NO_OF_SAMPLES_SHAKE_DETECTION = (int) (SHAKE_DETECTION_CHECK_TIME_IN_SEC * SAMPLING_RATE_IN_HZ);
    TUNE_CONVOLVE_OUTPUT = AntiShakeLibraryUtils.getPropertyValue(ConfigEnum.TUNE_CONVOLVE_OUTPUT, properties);
  }

  /**
   * @return earliestAccelerometerDataIndex
   */
  int getEarliestAccelerometerDataIndex() {
    earliestAccelerometerDataIndex = getCircularBuffer().getReadPointer();
    return earliestAccelerometerDataIndex;
  }

  /**
   * @param earliestAccelerometerDataIndex1
   */
  void setEarliestAccelerometerDataIndex(int earliestAccelerometerDataIndex1) {
    earliestAccelerometerDataIndex = earliestAccelerometerDataIndex1;
  }

  /**
   * @return impulseResponseSamples
   */
  ArrayList<Double> getImpulseResponseSamples() {
    if (impulseResponseSamples == null) {
      impulseResponseSamples = new ArrayList<Double>();
    }
    return impulseResponseSamples;
  }

  /**
   * @return accelerometerValues
   */
  private ArrayList<Coordinate> getAccelerometerValues() {
    if (accelerometerValues == null) {
      accelerometerValues = new ArrayList<Coordinate>();
    }
    accelerometerValues.clear();
    accelerometerValues.addAll(getCircularBuffer().getList());
    return accelerometerValues;
  }

  /**
   * @return responseSamples
   */
  private ArrayList<Coordinate> getResponseSamples() {
    if (responseSamples == null) {
      responseSamples = new ArrayList<Coordinate>(NO_OF_SAMPLES);
    }
    return responseSamples;
  }

  /**
   * @return latestAccelerometerDataIndex
   */
  private int getLatestAccelerometerDataIndex() {
    int wp = getCircularBuffer().getWritePointer();
    latestAccelerometerDataIndex = wp == 0 ? NO_OF_SAMPLES - 1 : wp - 1;
    return latestAccelerometerDataIndex;
  }

  /**
   * @param latestAccelerometerDataIndex1
   */
  void setLatestAccelerometerDataIndex(int latestAccelerometerDataIndex1) {
    latestAccelerometerDataIndex = latestAccelerometerDataIndex1;
  }

  /**
   * @return circularBuffer
   */
  private CircularBuffer getCircularBuffer() {
    if (circularBuffer == null) {
      circularBuffer = new CircularBuffer(NO_OF_SAMPLES);
    }
    return circularBuffer;
  }

  /**
   * @return tunedResponseSamples
   */
  ArrayList<Coordinate> getTunedResponseSamples() {
    if (tunedResponseSamples == null) {
      tunedResponseSamples = new ArrayList<Coordinate>(NO_OF_SAMPLES);
    }
    return tunedResponseSamples;
  }

  /**
   * @param tunedResponseSamples
   */
  void setTunedResponseSamples(ArrayList<Coordinate> tunedResponseSamples) {
    this.tunedResponseSamples = tunedResponseSamples;
  }

  /**
   * @return properties
   */
  public Properties getProperties() {
    return properties;
  }
}
