package com.divitech.postaShqiptare;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.serialport.api.MyApp;
import android.serialport.api.SerialPortFinder;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.smartdevicesdk.cscreen.CustomerScreenHelperPC900;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Copyright DIVITECH ICT, 2017.
 */

public class CustomerScreenActivity extends Activity implements View.OnClickListener {
    CustomerScreenHelperPC900 cs;
    ImageView imageView1;
    Spinner spinner_name, spinner_baud;
    String device = "/dev/ttyMT0";// MainActivity.devInfo.getPrinterSerialport();
    int baudrate = 115200;// MainActivity.devInfo.getPrinterBaudrate();
    Button button_open;
    CheckBox checkBox1;
    ThreadAuto _thAuto;
    int[] colorArray = new int[]{Color.BLUE, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.WHITE};
    int colorIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cscreen);

        MyApp mApplication = (MyApp) getApplication().getApplicationContext();
        SerialPortFinder mSerialPortFinder = mApplication.mSerialPortFinder;
        String[] entries = mSerialPortFinder.getAllDevices();
        final String[] entryValues = mSerialPortFinder.getAllDevicesPath();

        Button button_open = (Button) findViewById(R.id.button_open);
        button_open.setOnClickListener(this);

        Button button_dot = (Button) findViewById(R.id.button_dot);
        button_dot.setOnClickListener(this);

        Button button_rgb565 = (Button) findViewById(R.id.button_rgb565);
        button_rgb565.setOnClickListener(this);

//        Button button_rgb565_location = (Button) findViewById(R.id.button_rgb565_location);
//        button_rgb565_location.setOnClickListener(this);
//
//        Button button_updatelogo = (Button) findViewById(R.id.button_updatelogo);
//        button_updatelogo.setOnClickListener(this);

        checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    _thAuto=new ThreadAuto();
                    _thAuto.start();
                }else{
                    if(_thAuto!=null)
                    {
                        _thAuto.interrupt();
                        _thAuto=null;
                    }
                }
            }
        });

        imageView1 = (ImageView) findViewById(R.id.imageView1);

        /*spinner_name = (Spinner) findViewById(R.id.spinner_serialport_name);
        spinner_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                device = entryValues[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_baud = (Spinner) findViewById(R.id.spinner_serialport_baud);
        spinner_baud.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String dataStr = spinner_baud.getItemAtPosition(position)
                        .toString();
                baudrate = Integer.parseInt(dataStr);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_spinner_item, entries);
//        spinner_name.setAdapter(adapter);
//        final String[] baudValues = getResources().getStringArray(
//                R.array.baudrates_value);
//        adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_spinner_item, baudValues);
//        spinner_baud.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        cs = new CustomerScreenHelperPC900(device, baudrate);

        if (cs.open()) {
            cs.openBackLight((byte) 0x01);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        cs.openBackLight((byte) 0x00);
        cs.close();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        cs.openBackLight((byte) 0x00);
        cs.close();
        if(_thAuto!=null)
        {
            _thAuto.interrupt();
            _thAuto=null;
        }

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_open:
                if (cs != null && !cs.mSerialPort.isOpen) {
                    cs = new CustomerScreenHelperPC900(device, baudrate);
                    if (cs.open()) {
                        button_open.setText("ClOSE");
                    }
                } else {
                    if (cs != null) {
                        cs.close();
                        button_open.setText("OPEN");
                    }
                }
                break;
            case R.id.button_dot:
                if (colorIndex < colorArray.length - 1) {
                    colorIndex++;
                } else {
                    colorIndex = 0;
                }
                Bitmap bm = getBitmap();
                if (cs.ShowDotImage(colorArray[colorIndex], Color.BLACK, bm)) {
                    imageView1.setImageBitmap(bm);
                }
                break;
            case R.id.button_rgb565:
                Bitmap bmRGB565 = BitmapFactory.decodeResource(getResources(),
                        R.drawable.posta_shqiptare);
                if (cs.ShowRGB565Image(bmRGB565)) {
                    imageView1.setImageBitmap(bmRGB565);
                }
                break;
/*            case R.id.button_rgb565_location:
                bmRGB565 = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher_round);
                boolean flg = cs.ShowRGB565ImageCenter(bmRGB565);
                if (flg) {
                    imageView1.setImageBitmap(bmRGB565);
                }
                break;
            *//*case R.id.button_updatelogo:
                bmRGB565 = BitmapFactory.decodeResource(getResources(),
                        R.drawable.fj);
                if (cs.UpdateLogo(bmRGB565)) {
                    imageView1.setImageBitmap(bmRGB565);
                }*//*
                break;*/
            default:
                break;
        }
    }

    private Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(480, 272, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(80);

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String ss = df.format(new Date());
        canvas.drawColor(Color.WHITE);
        canvas.drawText(ss, 10, 100, paint);
        // imageView1.setImageBitmap(bitmap);
        return bitmap;
    }

    public class ThreadAuto extends Thread {
        @Override
        public void run() {
            super.run();
            while (!interrupted()) {
                if (colorIndex < colorArray.length - 1) {
                    colorIndex++;
                } else {
                    colorIndex = 0;
                }
                Bitmap bm = getBitmap();
                if (cs!=null&&cs.ShowDotImage(colorArray[colorIndex], Color.BLACK, bm)) {

                }
                SystemClock.sleep(2000);
            }
        }
    }
}