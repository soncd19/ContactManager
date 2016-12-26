package com.database;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Cao Dinh Son on 11/16/2016.
 */

public class BlockCall {

    private long id;

    private String number;

    private Context mContext;

    public ArrayList<String> mListBlockCall = new ArrayList<String>();

    public BlockCall(Context context) {
        this.mContext = context;
        initListBlockCall();
    }

    private void initListBlockCall(){
        mListBlockCall = BlackListManager.getInstance().getAllNumberBlock();
    }

    public ArrayList<String> listBlockCall(){
        return mListBlockCall;
    }
}
