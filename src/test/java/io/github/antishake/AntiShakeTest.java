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
  public void testConvolve() {
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
    Coordinate[] expectedResponseArray = new Coordinate[]{
      new Coordinate(3d, 3d, 3d),
      new Coordinate(8d, 8d, 8d),
      new Coordinate(11d, 11d, 11d),
      new Coordinate(9d, 9d, 9d),
      new Coordinate(7d, 7d, 7d),
      new Coordinate(3d, 3d, 3d),
      new Coordinate(1d, 1d, 1d)
    };
    testResponseSamples.addAll(Arrays.asList(expectedResponseArray));
    antiShakeImpl.convolve(impulseResponseSamples, accelerometerValues, responseSamples);
    Assert.assertEquals(testResponseSamples, responseSamples);
  }

  @Test
  public void testCircularBuffer() {
    CircularBuffer cb = new CircularBuffer(201);
    Coordinate element = new Coordinate(2, 5, 7);
    cb.add(element);
    Assert.assertEquals(1, cb.getWritePointer());
    Assert.assertEquals(0, cb.getReadPointer());
    //when the buffer execeds 201 values it should return to initial poisition of the block and  should start from there read pointer should move according to write pointer
    for (int i = 0; i < 356; i++) {
      cb.add(element);
    }
    Assert.assertEquals(156, cb.getWritePointer());
    Assert.assertEquals(157, cb.getReadPointer());

  }

  @Test
  public void testTune() {
    ArrayList<Coordinate> convolvedResponseSamples = new ArrayList<Coordinate>();
    Coordinate[] convolvedResponseArray = new Coordinate[]{new Coordinate(3d, 3d, 3d), new Coordinate(8d, 8d, 8d),
      new Coordinate(11d, 11d, 11d), new Coordinate(9d, 9d, 9d), new Coordinate(7d, 7d, 7d),
      new Coordinate(3d, 3d, 3d), new Coordinate(1d, 1d, 1d)};
    convolvedResponseSamples.addAll(Arrays.asList(convolvedResponseArray));

    ArrayList<Coordinate> expectedTunedResponseSamples = new ArrayList<Coordinate>();
    Coordinate[] expectedTunedResponseArray = new Coordinate[]{
      new Coordinate(3d * AntiShake.TUNE_CONVOLVE_OUTPUT, 3d * AntiShake.TUNE_CONVOLVE_OUTPUT,
        3d * AntiShake.TUNE_CONVOLVE_OUTPUT),
      new Coordinate(8d * AntiShake.TUNE_CONVOLVE_OUTPUT, 8d * AntiShake.TUNE_CONVOLVE_OUTPUT,
        8d * AntiShake.TUNE_CONVOLVE_OUTPUT),
      new Coordinate(11d * AntiShake.TUNE_CONVOLVE_OUTPUT, 11d * AntiShake.TUNE_CONVOLVE_OUTPUT,
        11d * AntiShake.TUNE_CONVOLVE_OUTPUT),
      new Coordinate(9d * AntiShake.TUNE_CONVOLVE_OUTPUT, 9d * AntiShake.TUNE_CONVOLVE_OUTPUT,
        9d * AntiShake.TUNE_CONVOLVE_OUTPUT),
      new Coordinate(7d * AntiShake.TUNE_CONVOLVE_OUTPUT, 7d * AntiShake.TUNE_CONVOLVE_OUTPUT,
        7d * AntiShake.TUNE_CONVOLVE_OUTPUT),
      new Coordinate(3d * AntiShake.TUNE_CONVOLVE_OUTPUT, 3d * AntiShake.TUNE_CONVOLVE_OUTPUT,
        3d * AntiShake.TUNE_CONVOLVE_OUTPUT),
      new Coordinate(1d * AntiShake.TUNE_CONVOLVE_OUTPUT, 1d * AntiShake.TUNE_CONVOLVE_OUTPUT,
        1d * AntiShake.TUNE_CONVOLVE_OUTPUT)};
    expectedTunedResponseSamples.addAll(Arrays.asList(expectedTunedResponseArray));

    antiShakeImpl.tune(convolvedResponseSamples);
    Assert.assertEquals(expectedTunedResponseSamples, antiShakeImpl.getTunedResponseSamples());
  }

  @Test
  public void testCalculateImplulseResponse() {
    double time = 0.02;
    double impulseResponse = time * Math.exp(-(time * Math.sqrt(AntiShake.SPRING_CONSTANT)));
    Assert.assertEquals(impulseResponse, antiShakeImpl.calculateImplulseResponse(time), 0.0001);
  }

  @Override
  public void onTranslationVectorReceived(ArrayList<Coordinate> responseSamples) {
    // TODO Auto-generated method stub

  }
}