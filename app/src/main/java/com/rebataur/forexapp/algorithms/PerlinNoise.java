package com.rebataur.forexapp.algorithms;

public class PerlinNoise {
    private double current;
    private double first;
    private double step;
    private double min;
    private double max;

    public PerlinNoise(double start, double step, double min, double max) {
        this.current = start;
        this.first = start;
        this.step = step;
        this.min = min;
        this.max = max;
    }

    public double next() {
        if (Math.random() > 0.5) {
            current += (Math.random() * step);
        } else{
            current -= (Math.random() * step);
        }
        if(current >= max)
            current = 100 - (Math.random() * step);
        else if(current <= min)
            current = 0 + (Math.random() * step);

        return current;
    }

    public void reset(){
        current = first;
    }

}
