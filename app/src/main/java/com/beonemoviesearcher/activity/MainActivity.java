package com.beonemoviesearcher.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.beoneaid.api.IBeoneAidService;
import com.beoneaid.api.IBeoneAidServiceCallback;
import com.beonemoviesearcher.R;
import com.beonemoviesearcher.dao.MovieInfo;
import com.beonemoviesearcher.util.JsonParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    private final String TAG = "MainActivity";
    /**
     *  界面控件绑定及界面相关属性
     */
    @BindView(R.id.tv_show_info)
    TextView tvShowInfo;
    @BindView(R.id.ll_1)
    LinearLayout ll1;
    @BindView(R.id.iv_1)
    ImageView iv1;
    @BindView(R.id.tv_1)
    TextView tv1;

    @BindView(R.id.ll_2)
    LinearLayout ll2;
    @BindView(R.id.iv_2)
    ImageView iv2;
    @BindView(R.id.tv_2)
    TextView tv2;

    @BindView(R.id.ll_3)
    LinearLayout ll3;
    @BindView(R.id.iv_3)
    ImageView iv3;
    @BindView(R.id.tv_3)
    TextView tv3;

    private List<MovieInfo> movieList;
    private int movListIndex = 0;

    /**
     *  生命周期
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);
        //绑定BeoneAid服务
        final Intent intent = new Intent();
        intent.setAction("com.beoneaid.api.IBeoneAidService");
        final Intent eintent = new Intent(createExplicitFromImplicitIntent(this,intent));
        bindService(eintent,serviceConnection, Service.BIND_AUTO_CREATE);

        initReqQue();

    }

    @Override
    protected void onStart() {
        try {
            if (iBeoneAidService != null){
                iBeoneAidService.setMode(0);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        serviceConnection = null;
        super.onDestroy();
    }
    /**
     *  命令解析函数
     */

    private void praseOrderByModeMovie(String order){
        if (order.contains("播放")) {
            if (movieList == null || movieList.size() == 0) {
                try {
                    iBeoneAidService.startSpeaking("请先搜索电影");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return;
            }
            int index;
            if (order.contains("1") || order.contains("一")) {
                index = movListIndex;
            } else if (order.contains("2") || order.contains("二")) {
                index = movListIndex + 1;
            } else if (order.contains("3") || order.contains("三")) {
                index = movListIndex + 2;
            } else {
                index = movListIndex + movieList.size();//下标越界
            }
            //例外情况
            if (order.equals("播放")){
                index = movListIndex;
            }
            if (index >= movieList.size()) {
                try {
                    iBeoneAidService.startSpeaking("您说错了吧");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return;
            }
            String idString = movieList.get(index).getId() + "";
            try{
                Intent intent = new Intent("com.tv.kuaisou.action.DetailActivity");
                intent.setPackage("com.tv.kuaisou");
                intent.putExtra("id", idString);
                startActivity(intent);
                iBeoneAidService.setMode(4);
            }catch (Exception e){
                try {
                    iBeoneAidService.startSpeaking("没有安装影视快搜，请安装");
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }

        } else if (order.indexOf("搜索") == 0) {
            String movName = order.substring(order.indexOf("搜索") + 2, order.length());
            searchMovie(movName);
        } else if (order.contains("下一") || order.contains("向后")) {
            if (movieList == null || movieList.size() == 0) {
                try {
                    iBeoneAidService.startSpeaking("请先搜索电影");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return;
            }
            movListIndex += 3;
            showMoveResult();
        } else if (order.contains("上一") || order.contains("向前")) {
            if (movieList == null || movieList.size() == 0) {
                try {
                    iBeoneAidService.startSpeaking("请先搜索电影");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return;
            }
            movListIndex -= 3;
            if (movListIndex < 0) {
                movListIndex = 0;
            }
            showMoveResult();
        } else if (order.equals("清空")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clearMovieShow();
                }
            });

        }
    }

    /**
     *  界面控制函数
     */
    private void searchMovie(String movName) {
        if (movieList != null) {
            movieList.clear();
        }
        movListIndex = 0;
        try {
            iBeoneAidService.startSpeaking("正在为你查找《" + movName + "》相关的内容");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String codes = null;
        try {
            codes = URLEncoder.encode(movName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = getString(R.string.search_movie_url) + codes;
        Log.d(TAG, "searchMovie: " + url);
        StringRequest stringRequest = new StringRequest(url, RsListener, RsErrorListener);
        mQueue.add(stringRequest);
    }

    private void showMoveResult() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (movieList.size() - movListIndex <= 0) {
                    try {
                        iBeoneAidService.startSpeaking("没有下一组了");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    movListIndex = movListIndex - 3;
                    return;
                }
//        if (speak) {
//            startTtsOutput("现在显示第" + (movListIndex / 3 + 1) + "组结果");
//        }
                clearMovieShow();
                if ((movieList.size() - movListIndex) >= 3) {
                    ll1.setVisibility(View.VISIBLE);
                    ll2.setVisibility(View.VISIBLE);
                    ll3.setVisibility(View.VISIBLE);
                    tv1.setText(movieList.get(movListIndex).getTitle());
                    tv2.setText(movieList.get(movListIndex + 1).getTitle());
                    tv3.setText(movieList.get(movListIndex + 2).getTitle());
                    ImageRequest imageRequest1 = new ImageRequest(
                            movieList.get(movListIndex).getPic(),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    iv1.setImageBitmap(response);
                                }
                            }, 0, 0, Bitmap.Config.RGB_565, RsErrorListener);
                    ImageRequest imageRequest2 = new ImageRequest(
                            movieList.get(movListIndex + 1).getPic(),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    iv2.setImageBitmap(response);
                                }
                            }, 0, 0, Bitmap.Config.RGB_565, RsErrorListener);
                    ImageRequest imageRequest3 = new ImageRequest(
                            movieList.get(movListIndex + 2).getPic(),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    iv3.setImageBitmap(response);
                                }
                            }, 0, 0, Bitmap.Config.RGB_565, RsErrorListener);
                    mQueue.add(imageRequest1);
                    mQueue.add(imageRequest2);
                    mQueue.add(imageRequest3);
                } else if ((movieList.size() - movListIndex) == 2) {
                    ll1.setVisibility(View.VISIBLE);
                    ll3.setVisibility(View.VISIBLE);
                    tv1.setText(movieList.get(movListIndex).getTitle());
                    tv3.setText(movieList.get(movListIndex + 1).getTitle());
                    ImageRequest imageRequest1 = new ImageRequest(
                            movieList.get(movListIndex).getPic(),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    iv1.setImageBitmap(response);
                                }
                            }, 0, 0, Bitmap.Config.RGB_565, RsErrorListener);
                    ImageRequest imageRequest2 = new ImageRequest(
                            movieList.get(movListIndex + 1).getPic(),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    iv3.setImageBitmap(response);
                                }
                            }, 0, 0, Bitmap.Config.RGB_565, RsErrorListener);
                    mQueue.add(imageRequest1);
                    mQueue.add(imageRequest2);
                } else if ((movieList.size() - movListIndex) == 1) {
                    ll2.setVisibility(View.VISIBLE);
                    tv2.setText(movieList.get(movListIndex).getTitle());
                    ImageRequest imageRequest1 = new ImageRequest(
                            movieList.get(movListIndex).getPic(),
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    iv2.setImageBitmap(response);
                                }
                            }, 0, 0, Bitmap.Config.RGB_565, RsErrorListener);
                    mQueue.add(imageRequest1);
                }
            }
        });


    }

    private void clearMovieShow() {
        ll1.setVisibility(View.GONE);
        ll2.setVisibility(View.GONE);
        ll3.setVisibility(View.GONE);
    }

    /**
     *  网络请求相关
     */
    private RequestQueue mQueue;

    private void initReqQue(){
        mQueue = Volley.newRequestQueue(this);
    }

    private Response.Listener<String> RsListener = new Response.Listener<String>() {
        @Override
        public void onResponse(final String response) {
            Log.d(TAG, "onResponse: " + response.toString());
            movieList = JsonParser.parseMovieResult(response);
            if (movieList != null) {
                if (movieList.size() > 0) {
                    showMoveResult();
                } else {
                    try {
                        iBeoneAidService.startSpeaking("没有搜索到结果，请重新搜索 ");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private Response.ErrorListener RsErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse: "+error.getMessage());
        }
    };
     /**
     *  远程服务绑定相关
     */
    private IBeoneAidService iBeoneAidService;
    private IBeoneAidServiceCallback iBeoneAidServiceCallback = new IBeoneAidServiceCallback.Stub() {
        @Override
        public void recognizeResultCallback(final String s) throws RemoteException {
            praseOrderByModeMovie(s);
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
//                iBeoneAidService.unregisterCallback(iBeoneAidServiceCallback);
                iBeoneAidService = null;

        }
    };
    //将隐式启动变成显式启动，Andriod5.0后必须使用这个方法
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}
