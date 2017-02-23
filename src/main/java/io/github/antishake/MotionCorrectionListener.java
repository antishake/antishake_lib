package io.github.antishake;

/**
 * Created by ruraj on 2/22/17.
 */
public interface MotionCorrectionListener {
  void onTranslationVectorReceived(float x, float y);
}
