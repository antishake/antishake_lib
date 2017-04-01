package io.github.antishake;

import java.util.ArrayList;

/**
 * Created by ruraj on 2/22/17.
 */
public interface MotionCorrectionListener {
//  void onTranslationVectorReceived(float x, float y);

  void onTranslationVectorReceived(ArrayList<Coordinate> responseSamples);

  void onDeviceSteady();
}