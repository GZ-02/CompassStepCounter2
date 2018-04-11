package com.example.georgia.compassstepcounter2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity implements SensorEventListener,StepListener{

    /********************************************Declaring Variables***************************************************/
    public String TAG="com.example.georgia.compassstepcounter";
    String floor;
    double distancePerStep;
    private SensorManager mySensorManager;
    private Sensor accelerometer,magnetometer;
    private  int azimuth,Avgazimuth;
    public int counter,NumberOfSteps=0;
    public String direction,direction1;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private StepDetector simpleStepDetector;
    double distanceTraveled=0.0;
    boolean exitLoop=false;
    SensorEventListener myListener1;
    ArrayList<Float> myListX =new ArrayList<Float>();
    ArrayList<Float> myListY =new ArrayList<Float>();
    ArrayList<Float> myListZ =new ArrayList<Float>();
    int Degrees,OldValue,i;

    //WIL REMOVE
    TextView txt_compass,txtSteps;
    ImageView compass_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Line to keep screen on permanently
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i(TAG,"Entered Particle filters");


        distancePerStep=2.3;


        //Assign values to the variables
        mySensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        counter=0;
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        compass_img = (ImageView) findViewById(R.id.img_compass);
        txt_compass = (TextView) findViewById(R.id.txt_azimuth);
        txtSteps=(TextView) findViewById(R.id.steps);

        //Register Listeners
        StartListeners();

        Runnable r=new Runnable(){
            @Override
            public void run(){
                Log.i(TAG,"Started thread");
                collectDirectionData();
            }
        };
        Thread myThread= new Thread(r);
        myThread.start();
    }


    public void collectDirectionData(){
        myListener1=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                    mLastAccelerometerSet = true;
                }
                else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                    mLastMagnetometerSet = true;
                    myListX.add(mLastMagnetometer[0]);
                    myListY.add(mLastMagnetometer[1]);
                    myListZ.add(mLastMagnetometer[2]);
                }
                if (mLastAccelerometerSet && mLastMagnetometerSet) {
                    SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
                    SensorManager.getOrientation(rMat, orientation);
                    azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
                }
                azimuth=Math.round(azimuth);
                compass_img.setRotation(-azimuth);
                direction=" " ;

                if (azimuth >= 320 && azimuth <= 360)
                    direction = "N";
                if (azimuth >=0  && azimuth <= 40)
                    direction = "N";
                if (azimuth >= 230 && azimuth <=310)
                    direction = "W";
                if (azimuth >= 140 && azimuth <= 220)
                    direction = "S";
                if (azimuth >= 50 && azimuth <=130)
                    direction = "E";

                txt_compass.setText(azimuth + "° " + direction);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        mySensorManager.registerListener(myListener1,accelerometer,SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(myListener1,magnetometer,SensorManager.SENSOR_DELAY_UI);
    }


    @Override
    public void onSensorChanged(SensorEvent SensorEvent) {
        if (SensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(SensorEvent.timestamp, SensorEvent.values[0], SensorEvent.values[1], SensorEvent.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //NOTHING HAPPENS HERE
    }

    @Override
    public void step(long timeNs) {
        NumberOfSteps++;
        txtSteps.setText("Steps: "+NumberOfSteps);
        distanceTraveled=NumberOfSteps*distancePerStep;
        Avgazimuth=alphaTrimm();
        Log.i(TAG,"Azimuth: " + String.valueOf(Avgazimuth));
        if (Avgazimuth >= 317 && azimuth <= 360) {
            direction1 = "N";
            Degrees=0;
            OldValue=0;
        }
        else if (Avgazimuth >=0  && azimuth <= 43) {
            direction1 = "N";
            Degrees=0;
            OldValue=0;

        }
        else if (Avgazimuth >= 227 && azimuth <=313) {
            direction1 = "W";
            Degrees=270;
            OldValue=270;
        }
        else if (Avgazimuth >= 137 && azimuth <= 223) {
            direction1 = "S";
            Degrees=180;
            OldValue=180;

        }
        else if (Avgazimuth >= 47 && azimuth <=133) {
            direction1 = "E";
            Degrees=90;
            OldValue=90;

        }
        else{
            Degrees=OldValue;
            direction1="Unknown";
        }
        Toast.makeText(this,"Direction: "+String.valueOf(direction1)+ " Degrees: "+String.valueOf(Degrees),Toast.LENGTH_SHORT).show();
        Log.i(TAG,"Steps: "+String.valueOf(NumberOfSteps)+", Distance: "+String.valueOf(distanceTraveled)+", Direction: "+String.valueOf(direction1));
        myListX.clear();
        myListY.clear();
        myListZ.clear();
    }


    @Override
    protected void onPause() {
        super.onPause();
        StopListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StartListeners();
    }

    @Override
    protected void onStop() {
        StopListeners();
        exitLoop=true;
        super.onStop();
    }

    //Method that registers listeners needed for motion model
    public void StartListeners(){
        mySensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    //Method that unregisters all listeners
    public void StopListeners(){
        mySensorManager.unregisterListener(MainActivity.this);
        mySensorManager.unregisterListener(myListener1);
        mySensorManager=null;
    }


    public int alphaTrimm(){
        int AverageValue=0;
        int newSize=0;
        float [] avergAxis=new float[3];
        Collections.sort(myListX);
        Collections.sort(myListY);
        Collections.sort(myListZ);
        float percentage=(5*myListX.size())/100;
        int percent=Math.round(percentage);
        Log.i(TAG,String.valueOf(myListX.size())+" "+String.valueOf(percent));
        for (i=0;i<percent;i++){
            newSize=myListX.size()-1;
            Log.i(TAG,"Entered Loop"+" "+String.valueOf(newSize));
            myListX.remove(newSize);
            myListY.remove(newSize);
            myListZ.remove(newSize);
            myListX.remove(i);
            myListY.remove(i);
            myListZ.remove(i);
            Log.i(TAG,String.valueOf(myListX.size()));
        }

        for (i=0;i<myListX.size();i++){
            avergAxis[0]=avergAxis[0]+myListX.get(i);
            avergAxis[1]=avergAxis[1]+myListY.get(i);
            avergAxis[2]=avergAxis[2]+myListZ.get(i);
        }
        avergAxis[0]=avergAxis[0]/myListX.size();
        avergAxis[1]=avergAxis[1]/myListY.size();
        avergAxis[2]=avergAxis[2]/myListZ.size();
        SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
        SensorManager.getOrientation(rMat, orientation);
        AverageValue = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        AverageValue=Math.round(AverageValue);
        return AverageValue;
    }

}




