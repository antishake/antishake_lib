package io.github.antishake;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
  private int readPtr;
  private int writePtr;
  private ArrayList<Coordinate> buffer;
  private int size;

  public CircularBuffer(int maxSize) {
    this.size = maxSize;
    this.buffer = new ArrayList<>(maxSize);
    for (int i = 0; i < maxSize; i++) {
      this.buffer.add(null);
    }
  }

  /**
   * Add a coordinate to the buffer
   */

  public void add(Coordinate value) {
    this.buffer.set(this.writePtr, value);
    this.writePtr = ((this.writePtr + 1) % this.size);

    if (this.readPtr == this.writePtr) {
      this.readPtr = ((this.readPtr + 1) % this.size);
    }
  }

  public Coordinate read() {
    if (this.readPtr == this.writePtr) {
      return null;
    }
    Coordinate res = this.buffer.get(this.readPtr);
    this.readPtr = ((this.readPtr + 1) % this.size);
    return res;
  }

  public Coordinate[] readLatest(int len) {
    throw new NotImplementedException();
  }

  public Coordinate[] readAll() {
    ArrayList<Coordinate> res = new ArrayList<>();
    Coordinate coord;
    while ((coord = read()) != null) {
      res.add(coord);
    }
    Coordinate[] arr = new Coordinate[res.size()];
    return res.toArray(arr);
  }

  public int getReadPointer() {
    return readPtr;
  }

  public int getWritePointer() {
    return writePtr;
  }

  public Coordinate[] toArray() {
    Coordinate[] array = new Coordinate[buffer.size()];
    return buffer.toArray(array);
  }
}

