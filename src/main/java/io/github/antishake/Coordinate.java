package io.github.antishake;

/**
 * Created by ruraj on 3/1/17.
 */
public class Coordinate {
  private double x;
  private double y;
  private double z;

  public Coordinate(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getZ() {
    return z;
  }

  public void setZ(double z) {
    this.z = z;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (!(o instanceof Coordinate)) return false;

    Coordinate other = (Coordinate) o;
    if (this.getX() != other.getX()) return false;
    if (this.getY() != other.getY()) return false;
    if (this.getZ() != other.getZ()) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Double.valueOf(getX()).hashCode() + Double.valueOf(getY()).hashCode() + Double.valueOf(getZ()).hashCode();
  }
}
