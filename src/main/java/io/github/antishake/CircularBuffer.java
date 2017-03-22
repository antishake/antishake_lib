package io.github.antishake;
import java.util.Arrays;

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
    public int read_pointer,write_pointer=0;
    public int size_buffer =201;
    double[] circularbuffer =new double[size_buffer];
    boolean buffer_full_once=false;
    public boolean isEmpty(){
        return read_pointer==write_pointer;
    }
    public void add(double element){
        if(read_pointer<size_buffer){
            if(!buffer_full_once){
                circularbuffer[read_pointer]=element;
                read_pointer++;
            }
            if(buffer_full_once){
                circularbuffer[read_pointer]=element;
                read_pointer++;
                write_pointer++;
            }

        }
        else{
            read_pointer=0;
            circularbuffer[read_pointer]=element;
            read_pointer++;
            buffer_full_once=true;

        }

    }
    public double[] get_elements(){
        if(!isEmpty()){
            return circularbuffer;
        }
        return circularbuffer;

    }


}

