package io.github.antishake;

import org.junit.*;

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
    antiShakeImpl.isShaking();
}
  @Test
  public void isConvolve() {
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
