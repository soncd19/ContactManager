package com.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cao Dinh Son on 11/16/2016.
 */

public class BlackListManager {
    private SQLiteDatabase sqLiteDatabase;
    private BlockCallDataHelper blockCallDataHelper;

    public static final String TAG = "BlackListManager";

    public static BlackListManager sInstance;

    private ArrayList<String> mListContactBlock = new ArrayList<String>();

    public static void init(Context context){
        sInstance = new BlackListManager(context);
    }

    public static BlackListManager getInstance(){
        return sInstance;
    }

    //SonCD: Contructer db black list
    public BlackListManager(Context context){
        blockCallDataHelper = new BlockCallDataHelper(context);
        open();
        new Thread(new Runnable() {
            @Override
            public void run() {
                initListContactBlock();
            }
        }).start();
    }

    private void initListContactBlock(){
        mListContactBlock = getAllNumberBlock();
    }

    public ArrayList<String> getListContactsBlock(){
        return mListContactBlock;
    }

    public boolean hasBlock(String number){
        boolean isBlock;
        if (mListContactBlock != null && mListContactBlock.size() > 0){
            isBlock = mListContactBlock.contains(number);
            return isBlock;
        }
        return false;
    }

    public void setListContactsBlock(String number, boolean isBlock){
        if (isBlock){
            mListContactBlock.add(number);
            insertToBlockCall(number);
        }else {
            mListContactBlock.remove(number);
            deleteBlockCall(number);
        }
    }

    private void open() throws SQLException{
        sqLiteDatabase = blockCallDataHelper.getWritableDatabase();
    }

    public void close(){
        blockCallDataHelper.close();
    }
    //SonCD: INSERT block call
    public void insertToBlockCall(String number){
        final ContentValues values = new ContentValues();
        values.put(BlockCallDataHelper.PHONE_NUMBER , number);
        sqLiteDatabase.insert(BlockCallDataHelper.TABLE_BLACKLIST, null, values);
    }
    //SonCD: Delete block call
    public void deleteBlockCall(String number){
        sqLiteDatabase.delete(BlockCallDataHelper.TABLE_BLACKLIST,
                BlockCallDataHelper.PHONE_NUMBER + " = '" + number + "'", null);
    }

    //SonCD: get all number block call
    public ArrayList<String> getAllNumberBlock(){
        ArrayList<String> blackList = new ArrayList<String>();
        String selectQuery = "SELECT * FROM " + BlockCallDataHelper.TABLE_BLACKLIST;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        if (cursor != null && cursor.getCount() > 0){
            cursor.moveToLast();
            do{
                try{
                    String number = cursor.getString(cursor.getColumnIndex(BlockCallDataHelper.PHONE_NUMBER));
                    blackList.add(number);
                }catch (Exception e){
                    Log.d(TAG, "SonCD: getAllNumberBlock() ERROR = "+ e.toString());
                }
            }while (cursor.moveToPrevious());
        }
        return blackList;
    }

}
