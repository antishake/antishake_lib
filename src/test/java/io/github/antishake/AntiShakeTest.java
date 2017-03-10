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
  }

  @Test
  public void isConvolve() {
      AntiShake.NO_OF_SAMPLES = 7;
      ArrayList<Double> impulseResponseSamples = new ArrayList<Double>();
      ArrayList<Double> accelerometerValues = new ArrayList<Double>();
      ArrayList<Double> testResponseSamples = new ArrayList<Double>();
      ArrayList<Double> responseSamples = new ArrayList<Double>();
      Double[] impulseArray = new Double[]{1d,2d,2d,1d,1d,0d,0d};
      impulseResponseSamples.addAll(Arrays.asList(impulseArray));
      Double[] accelerometerArray = new Double[]{3d,2d,1d,0d,0d,0d,0d};
      // accelerometerArray = new Double[]{0d,0d,0d,3d,2d,1d,0d}; //To test changing the index
      accelerometerValues.addAll(Arrays.asList(accelerometerArray));
      Double[] testResponseArray = new Double[]{3d,8d,11d,9d,7d,3d,1d};
      testResponseSamples.addAll(Arrays.asList(testResponseArray));
      antiShakeImpl.convolve(impulseResponseSamples,accelerometerValues,responseSamples);
      Assert.assertEquals(testResponseSamples,responseSamples);
  }

  @Test
  public void testTune() {
  }

  @Test
  public void testCalculateImplulseResponse(){
    double time = 0.02;
    double impulseResponse = time * Math.exp(-(time * Math.sqrt(AntiShake.SPRING_CONSTANT)));
    Assert.assertEquals(impulseResponse, antiShakeImpl.calculateImplulseResponse(time),0.0001);
  }

  public void onTranslationVectorReceived(float x, float y) {

  }
}
