package io.github.antishake;

import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ArrayList;
import java.util.*;

/**
 * Created by ruraj on 2/19/17.
 */
public class AntiShake {

  private final static Logger logger = Logger.getLogger(AntiShake.class);

  private static Properties properties;
  static double SPRING_CONSTANT;
  private static double DAMPING_RATIO;
  private static double CIRCULAR_BUFFER_IN_SEC;
  private static double SAMPLING_RATE_IN_HZ;
  private static ArrayList<Double> impulseResponseSamples;

  // To load the config.properties when the class is loaded
  static{
    InputStream is = null;
    try {
      properties = new Properties();
      is = ClassLoader.class.getResourceAsStream("/config.properties");
      properties.load(is);
      loadProperties();
    } catch (FileNotFoundException e) {
      logger.error(e);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  AntiShake(MotionCorrectionListener listener) {

  }

  AntiShake() {

  }
    /**
     * Checks whether the device is shaking or not
     * by comparing the sum of the values from accelerometer by the empirically determined threshold
     *
     **/

    public void isShaking() {

        {
            {
                int i;
               //For now defining threshold with a dummy value that is 0.5
                double threshold = 0.5 ;

                double[] inputArray = new double[200];
                for (i = 0; i < inputArray.length; i++) {
                    inputArray[i] = i;
                }
                //For now taking the dummy value for read pointer until the circular buffer is ready to implement
                int readPointer = 5;
                double newValue = 0;
                readPointer = i;
                int count = 0;
                for (i = readPointer; i < inputArray.length; i++) {

                    while (count < 90) {

                        if (readPointer < 0) {
                            readPointer = 199;
                        }
                        newValue = newValue + inputArray[readPointer] ;

                        readPointer--;
                        count++;
                        inputArray[i] = newValue;

                        if (newValue > threshold) {
                            System.out.println("Shake Detected");
                        } else {
                            System.out.println("NoShake detected");

                        }
                    }
                }
            }
        }
    }

  /**
   * Calculates the next motion correction using accelerometer reading
   * @param x
   * @param y
   * @param z
   */
  public void calculateTranslationVector(float x, float y, float z) {
    throw new NotImplementedException();
  }

  /**
   * Calculates the impulse response of the Spring-Mass-Damper system
   * (H(t) = t*e(-t*sqrt(k))) for {@link AntiShake#CIRCULAR_BUFFER_IN_SEC} seconds
   * with given {@link AntiShake#SAMPLING_RATE_IN_HZ}
   */
  private static void calculateImplulseResponse() {
    ArrayList<Double> impulseResponseSamples = getImpulseResponseSamples();
    double samplingRateInSeconds = (1.0d / SAMPLING_RATE_IN_HZ);
    int i = 0;
    double intervalInSeconds;
    do {
      intervalInSeconds = i * samplingRateInSeconds;
      impulseResponseSamples.add(calculateImplulseResponse(intervalInSeconds));
      i++;
    } while (intervalInSeconds < CIRCULAR_BUFFER_IN_SEC);
   }

  /**
   * Calculates impulse response of the Spring-Mass-Damper system (H(t) = t*e(-t*sqrt(k)))
   * for the given time
   * @param time
   * @return impulseResponse
   */
  static double calculateImplulseResponse(final double time) {
    Double impulseResponse = time * Math.exp(-(time * Math.sqrt(SPRING_CONSTANT)));
    return impulseResponse;
  }

  /**
   * Returns the value of the given key from the config.properties file
   * @param key
   * @return value
   */
  private static String getPropertyValue(String key){
    return properties.getProperty(key);
  }

  /**
   * Loads all the properties from config.properties file and assigns to appropriate static variables
   */
  private static void loadProperties() {
    SPRING_CONSTANT = Double.parseDouble(getPropertyValue("SPRING_CONSTANT"));
    DAMPING_RATIO = Double.parseDouble(getPropertyValue("DAMPING_RATIO"));
    CIRCULAR_BUFFER_IN_SEC = Double.parseDouble(getPropertyValue("CIRCULAR_BUFFER_IN_SEC"));
    SAMPLING_RATE_IN_HZ = Double.parseDouble(getPropertyValue("SAMPLING_RATE_IN_HZ"));
  }

  /**
   * Getter for impulseResponseSamples
   * @return impulseResponseSamples
   */
  static ArrayList<Double> getImpulseResponseSamples() {
    if(impulseResponseSamples == null) {
      impulseResponseSamples = new ArrayList<Double>();
    }
    return impulseResponseSamples;
  }
}

//Circular buffer to store and retrieve the accelerometer values

public int read,rear=0;                       // read pointer
public int size_buffer =201;
double[] circularbuffer =new double[size_buffer]; //initialized circular buffer using array list



    public boolean isEmpty(){
        return read==rear;
    }
    public void add(double x){
        int s=size();
        if(s==size_buffer-1){
            resize();
        }
        circularbuffer[rear++]=x;
        if(rear==size_buffer){
            rear=0;
        }
    }
    public int size(){
        return(size_buffer-read+rear)%size_buffer;
    }
    public Double remove(){
        if(isEmpty()){
            return null;
        }
        double x= circularbuffer[read++];
        if(read==size_buffer){
            read=0;
        }
        return x;

    }
    private void resize(){
        int s=size();
        size_buffer=2*size_buffer;
        int lastindex=s+1;
        double [] new_circularbuffer=new double[size_buffer];
        int i=0;
        while(s>0){
            s--;
            new_circularbuffer[i++]=new_circularbuffer[read++];
        }
        if(read==lastindex){
            read=0;

        }
        rear=i++;
        read=0;
        circularbuffer= new_circularbuffer;
    }
