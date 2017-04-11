package com.divitech.postaShqiptare;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.serialport.api.SerialPortParam;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.smartdevicesdk.cscreen.CustomerScreenHelperPC900;
import com.smartdevicesdk.device.DeviceInfo;
import com.smartdevicesdk.device.DeviceManage;
import com.smartdevicesdk.printer.PrintService;
import com.smartdevicesdk.printer.PrinterClassSerialPort;
import com.smartdevicesdk.printer.PrinterCommand;
import com.smartdevicesdk.ui.SystemControl;

import java.text.SimpleDateFormat;

/**
 * Copyright DIVITECH ICT, 2017.
 */

public class Webview extends AppCompatActivity {

    public static DeviceInfo devInfo = DeviceManage.getDevInfo("PC900");
    WebView web;
    Button print, refresh;
    PrinterClassSerialPort printerClass = null;
    CustomerScreenHelperPC900 cs;
    String device = "/dev/ttyMT0";
    private Bitmap btMap = null;
    private int baudrate = 115200;
    private boolean close_printer = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new CustomerScreenHelperPC900(device, baudrate);
        cs.open();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_webview);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        String ip = "http://192.168.0.85/index.html";

        web = (WebView) findViewById(R.id.webView);
        web.getSettings().setAllowFileAccessFromFileURLs(true);
        web.bringToFront();
        web.getSettings().setAllowFileAccess(true);
        web.setInitialScale(1);
        web.getSettings().setAllowUniversalAccessFromFileURLs(true);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        web.getSettings().setAllowContentAccess(true);
        web.getSettings().setAppCacheEnabled(true);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        web.getSettings().setAppCacheEnabled(false);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setSupportZoom(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        web.loadUrl("http://www.google.com");
        web.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {

            }

        });

        SerialPortParam.Path = "/dev/ttyMT0";////ttyS1
        SerialPortParam.Baudrate = 115200;

/*        if (cs != null && !cs.mSerialPort.isOpen) {
            cs = new CustomerScreenHelperPC900(device, baudrate);
            cs.open();
        }*/
        startPrinter();
        printerClass.device = device;
        printerClass.baudrate = baudrate;
        printerClass.open();
        printerClass.write(new byte[]{0x1b, 0x76});
        //Toast.makeText(this, "Printer Ready to print", Toast.LENGTH_SHORT).show();

        print = (Button) findViewById(R.id.print);
        print.setVisibility(View.VISIBLE);
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    printerClass.printText(GetPrintStr());
                } catch (Exception e) {
                    Log.e("Webview print", e.getMessage());
                }

            }
        });


        refresh = (Button) findViewById(R.id.refresh);
        refresh.setVisibility(View.VISIBLE);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bmRGB565 = BitmapFactory.decodeResource(getResources(),
                        R.drawable.posta_shqiptare);
                if (cs.ShowRGB565Image(bmRGB565)) {

                }
            }
        });

    }

    private void startPrinter() {

        Handler mhandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PrinterCommand.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        if (readBuf[0] == 0x13) {
                        } else if (readBuf[0] == 0x11) {
                            PrintService.isFUll = false;
                        } else {
                            String readMessage = new String(readBuf, 0, msg.arg1);
                            if (readMessage.contains("800"))// 80mm paper
                            {
                                PrintService.imageWidth = 72;
                                Toast.makeText(getApplicationContext(), "80mm",
                                        Toast.LENGTH_SHORT).show();
                            } else if (readMessage.contains("580"))// 58mm paper
                            {
                                PrintService.imageWidth = 48;
                                Toast.makeText(getApplicationContext(), "58mm",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case PrinterCommand.MESSAGE_STATE_CHANGE:// 6��l��״
                        switch (msg.arg1) {
                            case PrinterCommand.STATE_CONNECTED:// �Ѿ�l��
                                break;
                            case PrinterCommand.STATE_CONNECTING:// ����l��
                                Toast.makeText(getApplicationContext(),
                                        "STATE_CONNECTING", Toast.LENGTH_SHORT).show();
                                break;
                            case PrinterCommand.STATE_LISTEN:
                            case PrinterCommand.STATE_NONE:
                                break;
                            case PrinterCommand.SUCCESS_CONNECT:
                                printerClass.write(new byte[]{0x1b, 0x2b});// ����ӡ���ͺ�
                                Toast.makeText(getApplicationContext(),
                                        "SUCCESS_CONNECT", Toast.LENGTH_SHORT).show();
                                break;
                            case PrinterCommand.FAILED_CONNECT:
                                Toast.makeText(getApplicationContext(),
                                        "FAILED_CONNECT", Toast.LENGTH_SHORT).show();

                                break;
                            case PrinterCommand.LOSE_CONNECT:
                                Toast.makeText(getApplicationContext(), "LOSE_CONNECT",
                                        Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case PrinterCommand.MESSAGE_WRITE:

                        break;
                    case PrinterCommand.PERMIT_PRINTER:
                        String result = (String) msg.obj;
                        //Toast.makeText(getApplicationContext(),R.string.permit_printer, Toast.LENGTH_SHORT).show();
                        break;
                    case PrinterCommand.FORBID_PRINTER:
                        String forbid_print = (String) msg.obj;
                        //Toast.makeText(getApplicationContext(),R.string.forbid_print, Toast.LENGTH_SHORT).show();
                        break;
                    case PrinterCommand.TIMEOUT_PRINTER:
                        String print_timeout = (String) msg.obj;
                        //Toast.makeText(getApplicationContext(),R.string.open_print_function, Toast.LENGTH_SHORT).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        printerClass = new PrinterClassSerialPort(device, baudrate, mhandler);
    }


    public String GetPrintStr() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        StringBuilder sb = new StringBuilder();

        String titulli = "Posta Shqiptare";
        String kuponTatimor = "KUPON TATIMOR";
        String addresa = "Terminal Center, kthesa e Kamzes）";
        String saleID = "2016930233330";
        String item = "Produkti";
        Double price = 25.00;
        int count = 10;
        Double total = 0.00;
        Double pagesa = 5500.00;


        sb.append("     " + titulli + "     \n");
        sb.append("     " + kuponTatimor + "     \n");
        sb.append("Fatura nr:" + saleID + "\n");
        sb.append("********************************\n");

        sb.append("Produkti" + "\t\t" + "Sasia" + "\t" + "\t" + " " + "Cmimi" + "\n");
        for (int i = 0; i < count; i++) {
            Double xiaoji = (i + 1) * price;
            sb.append(item + (i + 1) + "\t\t" + (i + 1) + "\t" + price + "\t"
                    + xiaoji);
            total += xiaoji;

            if (i != (count))
                sb.append("\n");
        }

        sb.append("********************************\n");
        sb.append("Sasia: " + count + "\n");
        sb.append("Totali:   " + total + "\n");
        sb.append("Pagesa:" + "    " + pagesa + "\n");
        sb.append("Kusuri:" + "   " + (pagesa - total) + "\n");
        sb.append("********************************\n");
        sb.append("Adresa：" + addresa + "\n");
        sb.append("Telefon：355 4 2222 315\n");
        sb.append("********************************\n");
        sb.append("Kuponi Tatimor nr:" + saleID + "\n");
        sb.append("   " + date + "  " + "\n");
        sb.append("******Ju faleminderit******\r\n\n");
        return sb.toString();
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
        close_printer = true;
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (close_printer) {
            printerClass.close();
        }
    }

    @Override
    protected void onDestroy() {
        cs.openBackLight((byte) 0x00);
        cs.close();
        devInfo.closeModel();
        super.onDestroy();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        SystemControl.disableNotificationBar(this);
    }

}
