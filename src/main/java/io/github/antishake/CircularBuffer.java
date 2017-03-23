package io.github.antishake;

/**
 * Created by Geofe on 3/22/17.
 */
/*
 Created a  circular buffer class
    add fn will add the elements to the circular buffer
        -will maintain read pointer when updating with latest values
        -will maintain write pointer when updating with the latest values
 */
public class CircularBuffer {
  private int readPointer, writePointer = 0;
  private int sizeBuffer = 201;
  private Coordinate[] circularbuffer = new Coordinate[sizeBuffer];
  private boolean bufferFullOnce = false;

  public boolean isEmpty() {
    return readPointer == writePointer;
  }

  public void add(Coordinate element) {
    if (readPointer < sizeBuffer) {
      if (!bufferFullOnce) {
        circularbuffer[readPointer] = element;
        readPointer++;
      }
      if (bufferFullOnce) {
        circularbuffer[readPointer] = element;
        readPointer++;
        writePointer++;
      }
    } else {
      readPointer = 0;
      circularbuffer[readPointer] = element;
      readPointer++;
      bufferFullOnce = true;
    }
  }

  public Coordinate[] get_elements() {
    if (!isEmpty()) {
      return circularbuffer;
    }
    return circularbuffer;

  }

  public int getReadPointer() {
    return readPointer;
  }

  public int getWritePointer() {
    return writePointer;
  }
}

