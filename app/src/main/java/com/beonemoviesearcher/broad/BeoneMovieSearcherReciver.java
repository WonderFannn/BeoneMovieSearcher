package com.beonemoviesearcher.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.beonemoviesearcher.activity.MainActivity;

/**
 * Created by wangfan on 2017/10/20.
 */

public class BeoneMovieSearcherReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            return;
        }
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BeoneMovieShearcher", "==================== BeoneMovieShearcher Start ======================");
            try{
                Intent i = new Intent(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }catch (Exception e){
                Log.e("BeoneMovieShearcher", "onReceive: "+e.getMessage() );
            }
        }

    }
}
