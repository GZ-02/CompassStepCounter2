package com.example.georgia.compassstepcounter2;

// New listener dedicated to detecting when a step was taken
public interface StepListener {

    public void step(long timeNs);
}
