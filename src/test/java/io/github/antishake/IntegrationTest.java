package io.github.antishake;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ruraj on 3/25/17.
 */
public class IntegrationTest {
  private static AntiShake antiShake;
  private static BufferedReader testFileReader;
  private static FileWriter vectFileWriter;

  private static ArrayList<Coordinate> vectRows = new ArrayList<>();
  private static int vectRowIdx;

  private static boolean isShaking;
  private static int steadyCount;

  @BeforeClass
  public static void setup() throws URISyntaxException, IOException {
    antiShake = new AntiShake(motionCorrectionListener);

    testFileReader = new BufferedReader(
      new FileReader(
        IntegrationTest.class.getClassLoader().getResource("testdata.csv").toURI().getPath()
      )
    );

    File vectFile = new File("vect.csv");
    vectFile.delete();
    vectFileWriter = new FileWriter(vectFile);
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    testFileReader.close();
    vectFileWriter.close();
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

    Assert.assertEquals("Number of steady states are not equal", 4, steadyCount);

    for (Coordinate vectRow : vectRows) {
      write(vectFileWriter, vectRow);
    }
  }

  private void write(FileWriter writer, Coordinate coordinate) throws IOException {
    writer.write(coordinate.getX() + "," + coordinate.getY() + "," + coordinate.getZ() + "\n");
    writer.flush();
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
      vectRows.removeAll(vectRows.subList(vectRowIdx, vectRows.size()));
      vectRows.addAll(vectRowIdx++, responseSamples);
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
