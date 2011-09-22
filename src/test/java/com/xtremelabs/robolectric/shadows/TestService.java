package com.xtremelabs.robolectric.shadows;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

class TestService extends Service implements ServiceConnection {
    ComponentName name;
    IBinder service;
    ComponentName nameUnbound;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.name = name;
        this.service = service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        nameUnbound = name;
    }
}
