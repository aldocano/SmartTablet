package com.divitech.postaShqiptare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.serialport.api.SerialPortParam;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ListView;

import com.smartdevicesdk.cscreen.CustomerScreenHelperPC900;
import com.smartdevicesdk.device.DeviceInfo;
import com.smartdevicesdk.device.DeviceManage;
import com.smartdevicesdk.ui.SystemControl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = null;
    public static DeviceInfo devInfo = DeviceManage.getDevInfo("PC900");
    List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
    CustomerScreenHelperPC900 cs;
    String device = "/dev/ttyMT0";// MainActivity.devInfo.getPrinterSerialport();
    int baudrate = 115200;
    Button buttonScreen, buttonPrint, buttonWeb;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getOverflowMenu();
/*        listView = (ListView) findViewById(R.id.listView1);


        if (devInfo != null) {
            // 生成SimpleAdapter适配器对象
            SimpleAdapter mySimpleAdapter = new SimpleAdapter(this,
                    devInfo.getFunctionList(), R.layout.adapter_listview,
                    new String[] { "id", "name" }, new int[] {
                    R.id.textview_itemid, R.id.textview_itemname });
            listView.setAdapter(mySimpleAdapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(position);
                String titleStr = map.get("id");
                if (titleStr.equals("printer")){
                    intent.setClass(MainActivity.this, PrinterActivity.class);
                    startActivity(intent);
                }else if(titleStr.equals("cscreen")){
                    intent.setClass(MainActivity.this, CustomerScreenActivity.class);
                    startActivity(intent);
                }else {
                    intent.setClass(MainActivity.this, CustomerScreenActivity.class);
                    startActivity(intent);
                }
            }
        });*/

        buttonScreen = (Button) findViewById(R.id.buttonScreen);
        buttonScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setClass(MainActivity.this, CustomerScreenActivity.class);
                startActivity(intent);
            }
        });

        buttonPrint = (Button) findViewById(R.id.buttonPrint);
        buttonPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setClass(MainActivity.this, PrinterActivity.class);
                startActivity(intent);
            }
        });

        buttonWeb = (Button) findViewById(R.id.buttonWeb);
        buttonWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setClass(MainActivity.this, Webview.class);
                startActivity(intent);
            }
        });

        SerialPortParam.Path="/dev/ttyMT0";////ttyS1
        SerialPortParam.Baudrate=115200;
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
		/*
		 * Before all function calls, you must turn on the device
		 */
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        SystemControl.disableNotificationBar(this);
    }
}
