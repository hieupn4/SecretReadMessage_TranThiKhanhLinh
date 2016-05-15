package com.example.phamngochieu.secretreadmessage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by PhamNgocHieu on 06-Apr-16.
 * lớp này là lớp dùng để tạo CSDL lưu lại những tin nhắn đến và đi
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // tên của CSDL
    private static final String DB_NAME = "message";
    // Version của database
    private static final int DB_VERSON =2;
    public static SQLiteDatabase db;
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSON);
    }
    public static long count;

    @Override
    public void onCreate(SQLiteDatabase db) {
        // bảng này dùng để lưu tin nhắn đến và đi
        this.db = db;

        db.execSQL("CREATE TABLE STORE ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "PHONENUMBER TEXT, "
                + "TIME TEXT, "
                + "CONTENT TEXT, "
                + "SEND BOOLEAN);");
        // bảng này lưu thông tin danh bạ
        db.execSQL("CREATE TABLE CONTACT ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TIME TEXT, "
                + "CONTENT TEXT);");
        //bảng này dùng để lưu thông tin tin nhắn đi
        db.execSQL("CREATE TABLE STORESEND ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "PHONENUMBER TEXT, " // số máy gửi đến
                + "TIME TEXT, "       // ngày gửi tin nhắn
                + "CONTENT TEXT, "     // nội dung tin nhắn
                + "SEND BOOLEAN);");
        // bảng này dùng để lưu cuộc gọi đến
        db.execSQL("CREATE TABLE CALLINCOME ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "PHONENUMBER TEXT, "
                + "TIME TEXT);");
        // bảng này lưu thông tin cuộc gọi đi
        db.execSQL("CREATE TABLE CALLOUTCOME ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "PHONENUMBER TEXT, "
                + "TIME TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
