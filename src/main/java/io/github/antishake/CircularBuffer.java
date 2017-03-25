package io.github.antishake;


// Dummy class to integrate with Circular buffer
// Will be removed once circular buffer implementation is in place
public class CircularBuffer {

  public CircularBuffer(int nO_OF_SAMPLES) {
    // TODO Auto-generated constructor stub
  }

  public int getReadPointer() {
    // TODO Auto-generated method stub
    return 0;
  }

  public Coordinate[] getElements() {
    // TODO Auto-generated method stub
    Coordinate[] testResponseArray = new Coordinate[]{
      new Coordinate(3d, 3d, 3d),
      new Coordinate(8d, 8d, 8d),
      new Coordinate(11d, 11d, 11d),
      new Coordinate(9d, 9d, 9d),
      new Coordinate(7d, 7d, 7d),
      new Coordinate(3d, 3d, 3d),
      new Coordinate(1d, 1d, 1d)
    };
    return testResponseArray;
  }

  public int getWritePointer() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void add(Coordinate coordinate) {
    // TODO Auto-generated method stub

  }

}
