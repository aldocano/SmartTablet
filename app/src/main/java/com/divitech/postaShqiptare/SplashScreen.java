package com.divitech.postaShqiptare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.smartdevicesdk.cscreen.CustomerScreenHelperPC900;
import com.smartdevicesdk.device.DeviceInfo;
import com.smartdevicesdk.device.DeviceManage;

/**
 * Copyright DIVITECH ICT, 2017.
 */

public class SplashScreen extends AppCompatActivity {
    public static DeviceInfo devInfo = DeviceManage.getDevInfo("PC900");
    CustomerScreenHelperPC900 cs;
    String device = "/dev/ttyMT0";// MainActivity.devInfo.getPrinterSerialport();
    int baudrate = 115200;
    Button bt, bt1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        cs = new CustomerScreenHelperPC900(device, baudrate);
        cs.open();


/*        Bitmap bmRGB565 = BitmapFactory.decodeResource(getResources(),
                R.drawable.posta_shqiptare);
        if (cs.ShowRGB565Image(bmRGB565)) {
            Log.d("image", "shown");
        }*/

/*        bt =(Button) findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cs != null && !cs.mSerialPort.isOpen) {
                    cs = new CustomerScreenHelperPC900(device, baudrate);
                    if (cs.open()) {
                        bt.setText("ClOSE");
                    }
                } else {
                    if (cs != null) {
                        cs.close();
                        bt.setText("OPEN");
                    }
                }
            }
        });

        bt1 =(Button) findViewById(R.id.bt1);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bmRGB565 = BitmapFactory.decodeResource(getResources(),
                        R.drawable.posta_shqiptare);
                if (cs.ShowRGB565Image(bmRGB565)) {
                    Log.d("image", "shown");
                }
            }
        });*/


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashScreen.this, Webview.class);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();
            }
        }, 10000);
    }


    @Override
    protected void onResume() {
        devInfo.openModel();
        cs = new CustomerScreenHelperPC900(device, baudrate);

        if (cs.open()) {
            cs.openBackLight((byte) 0x01);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        cs.openBackLight((byte) 0x00);
        cs.close();
        devInfo.closeModel();
        super.onDestroy();
    }
}
