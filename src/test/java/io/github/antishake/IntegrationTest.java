package io.github.antishake;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by ruraj on 3/25/17.
 */
public class IntegrationTest {
  private static AntiShake antiShake;
  private static BufferedReader testFileReader;

  private static boolean isShaking;
  private static int steadyCount;

  @BeforeClass
  public static void setup() throws URISyntaxException, FileNotFoundException {
    antiShake = new AntiShake(motionCorrectionListener);

    testFileReader = new BufferedReader(
      new FileReader(
        IntegrationTest.class.getClassLoader().getResource("testdata.csv").toURI().getPath()
      )
    );
  }

  @Test
  public void testLibraryIntegrity() throws IOException {
    String line;
    // Ignore the first heading line
    testFileReader.readLine();

    while ((line = testFileReader.readLine()) != null) {
      Coordinate coord = strToCoord(line);
      antiShake.calculateTransformationVector(coord.getX(), coord.getY(), coord.getZ());
    }

    Assert.assertEquals("Number of steady states are not equal", 6, steadyCount);
  }

  private Coordinate strToCoord(String str) {
    String[] split = str.split(",");
    return new Coordinate(
      Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2])
    );
  }

  private static MotionCorrectionListener motionCorrectionListener = new MotionCorrectionListener() {
    @Override
    public void onTranslationVectorReceived(ArrayList<Coordinate> responseSamples) {
      isShaking = true;
      System.out.println("Received translation vectors of size: " + responseSamples.size());
    }

    @Override
    public void onDeviceSteady() {
      if (isShaking) {
        steadyCount++;
        isShaking = false;
      }
    }
  };
}
