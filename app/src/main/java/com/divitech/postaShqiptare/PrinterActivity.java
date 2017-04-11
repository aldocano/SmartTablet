package com.divitech.postaShqiptare;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.smartdevicesdk.adapter.SpinnerManage;
import com.smartdevicesdk.printer.BarcodeCreater;
import com.smartdevicesdk.printer.PrintService;
import com.smartdevicesdk.printer.PrinterClassSerialPort;
import com.smartdevicesdk.printer.PrinterCommand;
import com.smartdevicesdk.printer.PrinterInfo;
import com.smartdevicesdk.utils.TypeConversion;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright DIVITECH ICT, 2017.
 */

public class PrinterActivity extends Activity implements CompoundButton.OnCheckedChangeListener {
    protected static final String TAG = "PrintDemo";
    private static final int REQUEST_EX = 1;
    PrinterClassSerialPort printerClass = null;
    List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
    boolean isPrint = true;
    int times = 1500;// Automatic print time interval
    String thread = "readThread";
    String text = "abckefghijklmnopkrstuvwsyz1234567890打印测试\r\n";
    long startTimes = 0;
    long endTimes = 0;
    long timeSpace = 0;
    private int cutTimes = 1;
    private Thread autoprint_Thread;
    private ImageView iv = null;
    private boolean printFlag;
    private String picPath = "";
    private Bitmap btMap = null;
    private Button btnQrCode = null;
    private Button btnBarCode = null;
    private Button btnWordToPic = null;
    private Button btnUnicode;
    private Button btnOpenDevice;
    private Button btnPrint = null;
    private Button btnOpenPic = null;
    private Button btnPrintPic = null;
    private Button fatura = null;
    private TextView textViewState = null;
    private EditText et_input = null;
    private CheckBox checkBoxAuto = null;
    private Spinner spinner_device;
    private Spinner spinner_baudrate;
    private String device = "/dev/ttyMT0";
    private int baudrate = 115200;// 38400
    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                printerClass.device = device;
                printerClass.baudrate = baudrate;
                printerClass.open();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                printerClass.close();

            }
        }

    };
    /**
     * 允许/禁止打印标记
     */
    private boolean close_printer = true;
    private Handler hanler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    btnPrintPic.setEnabled(true);
                    btnOpenPic.setEnabled(true);
                    btnBarCode.setEnabled(true);
                    btnWordToPic.setEnabled(true);
                    btnQrCode.setEnabled(true);
                    btnPrint.setEnabled(true);
                    break;
                case 11:
                    btnPrintPic.setEnabled(false);
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 调整图片大小以适应打印机图片大小
     *
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        if (width >= newWidth) {
            float scaleWidth = ((float) newWidth) / width;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleWidth);
            Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                    height, matrix, true);
            return resizedBitmap;
        } else {
            Bitmap bitmap2 = Bitmap.createBitmap(newWidth, newHeight,
                    bitmap.getConfig());
            Canvas canvas = new Canvas(bitmap2);
            canvas.drawColor(Color.WHITE);

            canvas.drawBitmap(BitmapOrg, (newWidth - width) / 2, 0, null);

            return bitmap2;
        }
    }

    /**
     * 字符串转unicode编码的字节数组
     *
     * @param s
     * @return
     */
    static byte[] string2Unicode(String s) {
        try {
            byte[] bytes = s.getBytes("unicode");
            byte[] bt = new byte[bytes.length - 2];
            for (int i = 2, j = 0; i < bytes.length - 1; i += 2, j += 2) {
                bt[j] = (byte) (bytes[i + 1] & 0xff);
                bt[j + 1] = (byte) (bytes[i] & 0xff);
            }
            return bt;
        } catch (Exception e) {
            try {
                byte[] bt = s.getBytes("GBK");
                return bt;
            } catch (UnsupportedEncodingException e1) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getOverflowMenu();

        textViewState = (TextView) findViewById(R.id.textViewState);
        et_input = (EditText) findViewById(R.id.editText1);
        btnUnicode = (Button) findViewById(R.id.btnUnicode);
        btnPrint = (Button) findViewById(R.id.btnPrint);
        fatura = (Button) findViewById(R.id.printReceipts);
        et_input.setText(text);

        btnOpenPic = (Button) findViewById(R.id.btnOpenPic);
        btnPrintPic = (Button) findViewById(R.id.btnPrintPic);

        checkBoxAuto = (CheckBox) findViewById(R.id.checkBoxTimer);
        iv = (ImageView) findViewById(R.id.iv_test);

        btnQrCode = (Button) findViewById(R.id.btnQrCode);
        btnBarCode = (Button) findViewById(R.id.btnBarCode);

        btnWordToPic = (Button) findViewById(R.id.btnWordToPic);

        btnOpenDevice = (Button) findViewById(R.id.btnopendevice);

        spinner_device = (Spinner) findViewById(R.id.spinner1);
        spinner_device.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                device = spinner_device.getItemAtPosition(position).toString();
                if (printerClass.mSerialPort.isOpen) {
                    printerClass.close();
                    btnOpenDevice.setText(getResources().getString(
                            R.string.opendevice));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

        });
        spinner_baudrate = (Spinner) findViewById(R.id.spinner2);
        spinner_baudrate
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        String selectStr = spinner_baudrate.getItemAtPosition(
                                position).toString();
                        baudrate = Integer.parseInt(selectStr);
                        if (printerClass.mSerialPort.isOpen) {
                            printerClass.close();
                            btnOpenDevice.setText(getResources().getString(
                                    R.string.opendevice));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }

                });
        fatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printerClass.printText(GetPrintStr());
            }
        });

        btnOpenDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (printerClass.mSerialPort.isOpen) {
                    printerClass.close();
                    btnOpenDevice.setText(getResources().getString(
                            R.string.opendevice));
                } else {
                    printerClass.device = device;
                    printerClass.baudrate = baudrate;
                    printerClass.open();
                    printerClass.write(new byte[] { 0x1b, 0x76 });
                    btnOpenDevice.setText(getResources().getString(
                            R.string.closedevice));

                }
            }
        });

        if (btnQrCode != null) {
            btnQrCode.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String message = "http://www.google.com";
                    if (message.length() > 0) {
                        try {
                            message = new String(message.getBytes("utf8"));
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        btMap = BarcodeCreater.encode2dAsBitmap(message, 384, 384);
                        PrintService.imageWidth = 48;
                        iv.setImageBitmap(btMap);
                    }
                }
            });
        }
        if (btnWordToPic != null) {
            btnWordToPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String str = et_input.getText().toString();
                    btMap = Bitmap.createBitmap(384,
                            et_input.getLineCount() * 25, Bitmap.Config.ARGB_8888);
                    PrintService.imageWidth = 48;
                    Canvas canvas = new Canvas(btMap);
                    canvas.drawColor(Color.WHITE);
                    TextPaint textPaint = new TextPaint();
                    textPaint.setStyle(Paint.Style.FILL);
                    textPaint.setColor(Color.BLACK);
                    textPaint.setTextSize(25.0F);
                    StaticLayout layout = new StaticLayout(str, textPaint,
                            btMap.getWidth(), Layout.Alignment.ALIGN_NORMAL,
                            (float) 1.0, (float) 0.0, true);

                    layout.draw(canvas);
                    iv.setImageBitmap(btMap);

                }
            });
        }



        if (btnBarCode != null) {
            btnBarCode.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String message = "431D0E7D9CC19BC1FDAB7";
                    if (message.getBytes().length > message.length()) {
                        Toast.makeText(PrinterActivity.this, "create error",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (message.length() > 0) {
                        btMap = BarcodeCreater.creatBarcode(
                                PrinterActivity.this, message, 384, 50, false, BarcodeFormat.CODE_128);
                        PrintService.imageWidth = 48;
                        iv.setImageBitmap(btMap);
                    }
                }
            });
            if (btnPrint != null) {
                btnPrint.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String str = et_input.getText().toString();
                        try {

                            //检测打印机状态	Checking Printer Status
                            PrinterInfo pInfo=printerClass.getPrinterInfo();
                            String strState="";
                            if(pInfo.getPaper()==1)//是否缺纸Out of paper
                            {
                                strState=getResources().getString(R.string.str_printer_nopaper);
                            }
                            if(pInfo.getState()==1){//是否正在打印Printing in progress
                                strState+=","+getResources().getString(R.string.str_printer_printing);
                            }
                            if(pInfo.getTemperature()==1){//打印头温度是否过高Printhead temperature is too high
                                strState+=","+getResources().getString(R.string.str_printer_hightemperature);
                            }
                            if(pInfo.getVoltage()==1){//电压是否过低Voltage is too low
                                strState+=","+getResources().getString(R.string.str_printer_lowpower);
                            }

                            textViewState.setText(strState);

                            printerClass.printText(str);

                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
            if (btnUnicode != null) {
                btnUnicode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String str = et_input.getText().toString();
                        try {
                            printerClass.printUnicode(str);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
            if (btnOpenPic != null) {
                btnOpenPic.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        close_printer = false;
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_EX);
                    }
                });
            }
            if (btnPrintPic != null) {
                btnPrintPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread() {
                            public void run() {
                                if (btMap != null) {
                                    printerClass.printImage(btMap);
									/*
									 * Message msgMessage =
									 * hanler.obtainMessage(); msgMessage.what =
									 * 0; hanler.sendMessage(msgMessage);
									 */
                                }
                            }
                        }.start();
                        return;
                    }
                });
            }
        }

        btnBarCode.performClick();
        Handler mhandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PrinterCommand.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        Log.i(TAG, "readBuf:" + readBuf[0]);
                        if (readBuf[0] == 0x13) {
                            checkBoxAuto.setChecked(false);
                            PrintService.isFUll = true;
                            textViewState.setText(getResources().getString(
                                    R.string.str_printer_state)
                                    + ":"
                                    + getResources().getString(
                                    R.string.str_printer_bufferfull));
                            printFlag = false;
                        } else if (readBuf[0] == 0x11) {
                            PrintService.isFUll = false;
                            textViewState.setText(getResources().getString(
                                    R.string.str_printer_state)
                                    + ":"
                                    + getResources().getString(
                                    R.string.str_printer_buffernull));

                        }else {
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
                                printerClass.write(new byte[] { 0x1b, 0x2b });// ����ӡ���ͺ�
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
                        Toast.makeText(getApplicationContext(),R.string.permit_printer, Toast.LENGTH_SHORT).show();
                        break;
                    case PrinterCommand.FORBID_PRINTER:
                        String forbid_print = (String) msg.obj;
                        Toast.makeText(getApplicationContext(),R.string.forbid_print, Toast.LENGTH_SHORT).show();
                        break;
                    case PrinterCommand.TIMEOUT_PRINTER:
                        String print_timeout = (String) msg.obj;
                        Toast.makeText(getApplicationContext(),R.string.open_print_function, Toast.LENGTH_SHORT).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        printerClass = new PrinterClassSerialPort(device, baudrate, mhandler);

        // Auto Print 自动打印
        autoprint_Thread = new Thread() {
            public void run() {
                while (isPrint) {
                    startTimes =  0;
                    endTimes = 0;
                    timeSpace = 0;
                    startTimes = System.currentTimeMillis();
                    if (checkBoxAuto.isChecked()) {
                        // 打印logo
                        Bitmap bitmap = BitmapFactory.decodeResource(
                                getResources(), R.drawable.logo);
                        printerClass.printImage(bitmap);
                        bitmap.recycle();
                        bitmap.recycle();
                        bitmap.recycle();
                        // 打印一维码
                        bitmap = BarcodeCreater.creatBarcode(
                                PrinterActivity.this, "69587952556", 384, 150,
                                false, BarcodeFormat.CODE_128);
                        printerClass.printImage(bitmap);
                        bitmap.recycle();
                        printerClass.printText(GetPrintStr());

                        // 打印二维码
                        bitmap = BarcodeCreater.encode2dAsBitmap(
                                "http://weixin.qq.com/r/OUPk-OjEw7W8rayf9xYr",
                                200, 200);
                        printerClass.printImage(bitmap);
                        bitmap.recycle();
                        bitmap = null;
                        System.gc();

                        printerClass.printText("\r\n");
                    } else {
                        cutTimes = 0;
                    }
                }
            }
        };
        autoprint_Thread.start();

        //获取当前打印机的串口和波特率
        device = MainActivity.devInfo.getPrinterSerialport();
        baudrate = MainActivity.devInfo.getPrinterBaudrate();

        //设置默认串口和波特率
        SpinnerManage.setDefaultItem(spinner_device, device);
        SpinnerManage.setDefaultItem(spinner_baudrate, baudrate);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mBatInfoReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        close_printer = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(close_printer){
            printerClass.close();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBatInfoReceiver);
        super.onDestroy();
    }

    /**
     * 再次进入当前页面获取上一页面传递的值
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EX && resultCode == RESULT_OK
                && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            picPath = picturePath;
            iv.setImageURI(selectedImage);
            btMap = BitmapFactory.decodeFile(picPath);
            if (btMap.getHeight() > 384) {
                btMap = BitmapFactory.decodeFile(picPath);
                iv.setImageBitmap(resizeImage(btMap, 384, 384));

            }
            cursor.close();
        }

    }

    /**
     * 添加子菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Resources res = getResources();
        String[] cmdStr = res.getStringArray(R.array.cmd);
        for (int i = 0; i < cmdStr.length; i++) {
            String[] cmdArray = cmdStr[i].split(",");
            if (cmdArray.length == 2) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("title", cmdArray[0]);
                map.put("description", cmdArray[1]);
                menu.add(0, i, i, cmdArray[0]);
                listData.add(map);
            }
        }

        return true;
    }

    /**
     * 显示菜单
     */
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

    /**
     * 验证相关打印指令
     */
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        Map map = listData.get(item.getItemId());
        String cmd = map.get("description").toString();

        byte[] bt = TypeConversion.hexStringToBytes(cmd);
        printerClass.write(bt);
        printerClass.printText(map.get("title").toString());
        Toast toast = Toast.makeText(this, "send success！", Toast.LENGTH_SHORT);
        toast.show();
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


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
        Double fukuan = 500.00;


        sb.append("   " + titulli + "     \n");
        sb.append("   " + kuponTatimor + "     \n");
        sb.append("Fatura nr:" + saleID + "\n");
        sb.append("******************************\n");

        sb.append("Produkti" + "\t\t" + "Sasia" + "\t" + "\t" + "Cmimi" + "\n");
        for (int i = 0; i < count; i++) {
            Double xiaoji = (i + 1) * price;
            sb.append(item + (i + 1) + "\t\t" + (i + 1) + "\t" + price + "\t"
                    + xiaoji);
            total += xiaoji;

            if (i != (count))
                sb.append("\n");
        }

        sb.append("******************************\n");
        sb.append("Sasia: " + count + "\n");
        sb.append("Totali:   " + total + "\n");
        sb.append("Pagesa:" + "    " + fukuan + "\n");
        sb.append("Kusuri:" + "   " + (fukuan - total) + "\n");
        sb.append("******************************\n");
        sb.append("Adresa：" + addresa + "\n");
        sb.append("Telefon：355 4 2222 315\n");
        sb.append("******************************\n");
        sb.append("Kuponi Tatimor nr:" + saleID + "\n");
        sb.append(date + "  " + "\n");
        sb.append("******Ju faleminderit******\r\n\n");
        return sb.toString();
    }

//    static {
//        System.loadLibrary("serial_port");
//    }

}
