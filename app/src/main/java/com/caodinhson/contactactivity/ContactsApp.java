package com.caodinhson.contactactivity;

import android.app.Application;
import android.util.Log;

import com.database.BlackListManager;
import com.database.BlockCall;

/**
 * Created by Cao Dinh Son on 12/14/2016.
 */

public class ContactsApp extends Application {

    public static final String TAG = "ContactsApp";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SonCD: Vao ContactsApp dau tien");
        BlackListManager.init(this);
    }
}
