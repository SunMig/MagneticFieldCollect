package com.example.lenovo.magneticfieldcollect;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private Button bt1,bt2;
    private TextView tv1,tv2,tv3;
    private SensorManager sm;
    private Sensor msensor;
    private Sensor asensor;
    private Sensor vsensor;
    private Boolean doWrite=false;
    float[] accValues=new float[3];
    float[] magValues=new float[3];//磁场测量值
    float[] VecotorValues=new float[5];
    float[] rorate=new float[9];
    float[] value=new float[3];
    private int count=0;
    String fileName="magneticdata";
    String sdPath;
    private String[] needed_permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt1=(Button)findViewById(R.id.btn1);
        bt2=(Button)findViewById(R.id.btn2);
        sm=(SensorManager)getSystemService(SENSOR_SERVICE);
        msensor=sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //asensor=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        vsensor=sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sm.registerListener(this,msensor,CollectTime.COLLECT_NORMAL);
        sm.registerListener(this,vsensor,CollectTime.COLLECT_NORMAL);
        requestApplicationPermission();

        //设置点击监听
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doWrite=true;
                count++;
                fileName=fileName+"_"+count;

            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doWrite=false;
                fileName="magneticdata";
            }
        });
    }

    private void requestApplicationPermission() {
        needed_permission = new String[]{
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.READ_LOGS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        boolean permission_ok = true;
        for (String permission : needed_permission) {
            if (ContextCompat.checkSelfPermission(this,
                    permission) != PackageManager.PERMISSION_GRANTED) {
                permission_ok = false;
//                mTextView.append(String.valueOf(permission_ok)+"\n");
            }
        }
        if (!permission_ok) {
            ActivityCompat.requestPermissions(this, needed_permission, 1);
        }
    }

    private void WriteFileSdcard(String message) {
        try{
        //创建文件夹
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator;
            File file = new File(sdPath+FileName.str+File.separator);
            if(!file.exists()){
                file.mkdir();
            }
            //创建文件并写入
            File file1=new File(sdPath+FileName.str+File.separator+fileName+".txt");
            if (!file1.exists()) {
                file1.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file1,true);
            fos.write(message.getBytes());
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @RequiresApi(api=24)
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ROTATION_VECTOR:
                VecotorValues=event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magValues=event.values.clone();
                break;
            default:
                break;
        }
        String message =null;
        DecimalFormat df = new DecimalFormat("#,##0.0000");
        double Bx =magValues[0];//取绝对值
        double By =magValues[1];
        double Bz =magValues[2];
        double B = Math.sqrt(Bx * Bx + By * By + Bz * Bz);
        //double B_v=Math.sqrt(Bx*Bx+By*By);//水平强度
        //VecotorValues[0]是x*sin(a/2),VecotorValues[1]是y*sin(a/2),
        //VecotorValues[2]是z*sin(a/2),,VecotorValues[3]是cos(a/2),
        SensorManager.getRotationMatrix(rorate,null,accValues,magValues);
        SensorManager.getOrientation(rorate,value);
        Log.d("aaa"," "+B);
        //加时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String str = sdf.format(new Date());
        message = Bx +" "+ By +" "+Bz+" "+B+" "+VecotorValues[3]+" "+VecotorValues[0]+" "+VecotorValues[1]+" "+VecotorValues[2]+"\n";
        Log.d("mainactitvity:",message);
        if (doWrite) {
            WriteFileSdcard(message);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
