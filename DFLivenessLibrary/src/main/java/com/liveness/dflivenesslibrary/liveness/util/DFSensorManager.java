package com.liveness.dflivenesslibrary.liveness.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.content.Context.SENSOR_SERVICE;

public class DFSensorManager {

    private SensorManager mSM = null;
    public DFSensorManager(Context context) {
        mSM = (SensorManager) context.getSystemService(SENSOR_SERVICE);
    }

    public void registerListener(SensorEventListener listener) {
        mSM.registerListener(listener, mSM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        mSM.registerListener(listener, mSM.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
        mSM.registerListener(listener, mSM.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_UI);
        mSM.registerListener(listener, mSM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
    }

    public void unregisterListener(SensorEventListener listener) {
        mSM.unregisterListener(listener);
    }
}
