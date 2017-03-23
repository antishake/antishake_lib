package io.github.antishake;

import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ruraj on 3/1/17.
 */
public class AntiShakeTest implements MotionCorrectionListener {

  private static AntiShake antiShakeImpl;

  @BeforeClass
  public static void setup() {
    antiShakeImpl = new AntiShake();
  }

  @Test
  public void testCompensateGravity() {
  }

  @Test
  public void testIsShaking() {
    AntiShake.SHAKE_DETECTION_THRESHOLD = 10;
    ArrayList<Coordinate> accelerometerValues = new ArrayList<Coordinate>();
    Coordinate[] accelerometerArrayShake = new Coordinate[]{
      new Coordinate(3d, 3d, 1d),
      new Coordinate(2d, 2d, 2d),
      new Coordinate(1d, 1d, 1d),
      new Coordinate(0d, 6d, 0d),
      new Coordinate(0d, 1d, 0d),
      new Coordinate(0d, 0d, 0d),
      new Coordinate(0d, 0d, 0d)
    };
    accelerometerValues.addAll(Arrays.asList(accelerometerArrayShake));
    // aggregateX = 6d, aggregateY = 13d, aggregateZ = 4d - should give true for shaking as it is greater than threshold
    boolean isShaking = antiShakeImpl.isShaking(accelerometerValues);
    Assert.assertTrue(isShaking);
    Coordinate[] accelerometerArrayNoShake = new Coordinate[]{
      new Coordinate(3d, 3d, 1d),
      new Coordinate(2d, 2d, 2d),
      new Coordinate(1d, 1d, 1d),
      new Coordinate(0d, 0d, 0d),
      new Coordinate(0d, 1d, 0d),
      new Coordinate(0d, 0d, 0d),
      new Coordinate(0d, 0d, 0d)
    };
    accelerometerValues.clear();
    accelerometerValues.addAll(Arrays.asList(accelerometerArrayNoShake));
    // aggregateX = 6d, aggregateY = 7d, aggregateZ = 4d - should give false for shaking as either of the axis is greater than threshold
    isShaking = antiShakeImpl.isShaking(accelerometerValues);
    Assert.assertFalse(isShaking);
  }

  @Test
  public void testCalculateTransformationVector() {
    AntiShake.NO_OF_SAMPLES = 7;
    ArrayList<Double> impulseResponseSamples = new ArrayList<Double>();
    ArrayList<Coordinate> accelerometerValues = new ArrayList<Coordinate>();
    ArrayList<Coordinate> testResponseSamples = new ArrayList<Coordinate>();
    ArrayList<Coordinate> responseSamples = new ArrayList<Coordinate>();
    Double[] impulseArray = new Double[]{1d, 2d, 2d, 1d, 1d, 0d, 0d};
    impulseResponseSamples.addAll(Arrays.asList(impulseArray));
    Coordinate[] accelerometerArray = new Coordinate[]{
      new Coordinate(3d, 3d, 3d),
      new Coordinate(2d, 2d, 2d),
      new Coordinate(1d, 1d, 1d),
      new Coordinate(0d, 0d, 0d),
      new Coordinate(0d, 0d, 0d),
      new Coordinate(0d, 0d, 0d),
      new Coordinate(0d, 0d, 0d)
    };

    // accelerometerArray = new Double[]{0d,0d,0d,3d,2d,1d,0d}; //To test
    // changing the index.
    // change earliestAccelerometerDataIndex value to 3 in AntiShake.java (3
    // position is shifted from original array)
    /*
     * Coordinate[] accelerometerArray = new Coordinate[]{ new
		 * Coordinate(0d,0d,0d), new Coordinate(0d,0d,0d), new
		 * Coordinate(0d,0d,0d), new Coordinate(3d,3d,3d), new
		 * Coordinate(2d,2d,2d), new Coordinate(1d,1d,1d), new
		 * Coordinate(0d,0d,0d) };
		 */
    accelerometerValues.addAll(Arrays.asList(accelerometerArray));
    // Double[] testResponseArray = new Double[]{3d,8d,11d,9d,7d,3d,1d};
    Coordinate[] testResponseArray = new Coordinate[]{
      new Coordinate(3d, 3d, 3d),
      new Coordinate(8d, 8d, 8d),
      new Coordinate(11d, 11d, 11d),
      new Coordinate(9d, 9d, 9d),
      new Coordinate(7d, 7d, 7d),
      new Coordinate(3d, 3d, 3d),
      new Coordinate(1d, 1d, 1d)
    };
    testResponseSamples.addAll(Arrays.asList(testResponseArray));
    antiShakeImpl.calculateTransformationVector(impulseResponseSamples, accelerometerValues, responseSamples);
    Assert.assertEquals(testResponseSamples, responseSamples);
  }

  @Test
  public void testTune() {
  }

  @Test
  public void testCalculateImplulseResponse() {
    double time = 0.02;
    double impulseResponse = time * Math.exp(-(time * Math.sqrt(AntiShake.SPRING_CONSTANT)));
    Assert.assertEquals(impulseResponse, antiShakeImpl.calculateImplulseResponse(time), 0.0001);
  }

  public void onTranslationVectorReceived(float x, float y) {

  }
}