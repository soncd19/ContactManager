package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.database.BlackListManager;
import com.util.PermissionUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;


/**
 * Created by Cao Dinh Son on 11/16/2016.
 */

public class BlockCallListener extends BroadcastReceiver {

    private static final String TAG = "BlockCallListener";

    private String number;

    private BlackListManager mBlackListManager;

    private static final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";

    private ArrayList<String> listBlock = new ArrayList<String>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(ACTION_PHONE_STATE)){
            return;
        }else {
            number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            mBlackListManager = new BlackListManager(context);
            listBlock = mBlackListManager.getAllNumberBlock();
            if (listBlock.contains(number)){
                disconectPhone(context);
                return;
            }else {
                return;
            }
        }
    }
    //SonCD: dis connect incoming call
    private void disconectPhone(Context context){
        ITelephony telephonyService;
        TelephonyManager telephony = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        try{
            Class c = Class.forName(telephony.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            telephonyService = (ITelephony) m.invoke(telephony);
            telephonyService.endCall();
        }catch (Exception e){
            Log.d(TAG, "SonCD disconectPhone error = " + e.toString());
        }
    }
}
