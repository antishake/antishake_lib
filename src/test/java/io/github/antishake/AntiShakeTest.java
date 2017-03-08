package io.github.antishake;

import org.junit.*;

/**
 * Created by ruraj on 3/1/17.
 */
public class AntiShakeTest implements MotionCorrectionListener {

  private AntiShake antiShakeImpl;

  @BeforeClass
  public void setup() {
    antiShakeImpl = new AntiShake(this);
  }

  @Test
  public void testCompensateGravity() {
  }

  @Test
  public void testIsShaking() {
  }

  @Test
  public void isConvolve() {
  }

  @Test
  public void testTune() {
  }

  public void onTranslationVectorReceived(float x, float y) {

  }
}
