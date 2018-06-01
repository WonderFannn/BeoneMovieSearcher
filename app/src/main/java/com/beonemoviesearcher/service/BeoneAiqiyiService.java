package com.beonemoviesearcher.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beoneaid.api.IBeoneAidService;
import com.beoneaid.api.IBeoneAidServiceCallback;
import com.beonemoviesearcher.aiqiyi.AiqiyiController;
import com.beonemoviesearcher.aiqiyi.AiqiyiSpeakTextListener;
import com.beonemoviesearcher.broad.BroadcastManager;

/**
 * Created by wangfan on 2018/5/11
 */

public class BeoneAiqiyiService extends Service implements AiqiyiSpeakTextListener {

    private static final String TAG = "BeoneAiqiyiService";

    private static final String OPEN_FLAG = "open_movie_mode";
    private static final String CLOSE_FLAG = "close_movie_mode";

    private AiqiyiController mAiqiyiController;

    private Boolean needParseorder = true;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        BroadcastManager.sendBroadcast(BroadcastManager.ACTION_SIMULATE_KEY_DPAD_DOWN,null);
//        BroadcastManager.sendBroadcast(BroadcastManager.ACTION_SIMULATE_KEY_DPAD_DOWN,null);
//        BroadcastManager.sendBroadcast(BroadcastManager.ACTION_SIMULATE_KEY_DPAD_DOWN,null);
//        BroadcastManager.sendBroadcast(BroadcastManager.ACTION_SIMULATE_KEY_DPAD_DOWN,null);
        //绑定BeoneAid服务
        final Intent intent = new Intent();
        intent.setAction("com.beoneaid.api.IBeoneAidService");
        intent.setPackage("com.jinxin.beoneaid");
        bindService(intent,serviceConnection, Service.BIND_AUTO_CREATE);
        mAiqiyiController = new AiqiyiController(this);
        mAiqiyiController.setAiqiyiSpeakTextListener(this);
        mAiqiyiController.start();


    }


    @Override
    public void speakText(String text) {
        Log.d(TAG, "speakText: "+text);
        serviceSpeaking(text);
    }

    private void parseOrder(String s) {
        Log.d(TAG, "parseOrder: " + s);
        if (s.equals(OPEN_FLAG)){
            needParseorder = true;
            BroadcastManager.sendBroadcast(BroadcastManager.ACTION_SIMULATE_KEY_DPAD_DOWN,null);
            BroadcastManager.sendBroadcast(BroadcastManager.ACTION_SIMULATE_KEY_DPAD_DOWN,null);
            return;
        }
        if (s.equals(CLOSE_FLAG)){
            needParseorder = false;
            return;
        }

        if (needParseorder){
            mAiqiyiController.parseOrder(s);
        }
    }
    /**
     *  远程服务绑定相关
     */
    private IBeoneAidService iBeoneAidService;
    private IBeoneAidServiceCallback iBeoneAidServiceCallback = new IBeoneAidServiceCallback.Stub() {
        @Override
        public void recognizeResultCallback(final String s) throws RemoteException {
            Log.d(TAG, "recognizeResultCallback: "+s);
            parseOrder(s);
        }
    };


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iBeoneAidService = IBeoneAidService.Stub.asInterface(iBinder);
            try {
                iBeoneAidService.registerCallback(iBeoneAidServiceCallback);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", "onServiceConnected: wrong");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            iBeoneAidService = null;
            Log.e(TAG, "onServiceDisconnected: 服务断开了" );

        }
    };

    /**
     *  远程服务api
     */

    private void serviceSpeaking(String text){
        if (iBeoneAidService != null){
            try {
//                iBeoneAidService.startSpeaking(text);
                iBeoneAidService.startSpeakingWithoutRecognize(text);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            Log.e(TAG, "serviceSpeaking: service is null");
        }
    }

    private void serviceSetMode(int mode){
        if (iBeoneAidService != null){
            try {
                iBeoneAidService.setMode(mode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            Log.e(TAG, "serviceSetMode: service is null");
        }
    }


}
