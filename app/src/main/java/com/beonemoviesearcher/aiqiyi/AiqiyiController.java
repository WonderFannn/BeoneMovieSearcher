package com.beonemoviesearcher.aiqiyi;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.beonemoviesearcher.util.SwitchTimeNumberUtil;
import com.gala.tv.voice.VoiceClient;
import com.gala.tv.voice.VoiceEvent;
import com.gala.tv.voice.VoiceEventFactory;

/**
 * Created by wangfan on 2018/5/14.
 */

public class AiqiyiController {
    private static final String TAG = "AiqiyiController";

    private Context mContext;
    private VoiceClient mVoiceClient;
    private Boolean isConnected = false;

    public AiqiyiController(Context context){
        mContext = context;
        init();
    }
    private AiqiyiSpeakTextListener mAiqiyiSpeakTextListener = null;

    public void setAiqiyiSpeakTextListener(AiqiyiSpeakTextListener aiqiyiSpeakTextListener) {
        mAiqiyiSpeakTextListener = aiqiyiSpeakTextListener;
    }

    private void init() {
        VoiceClient.initialize(mContext,"com.qiyi.video.jinxinzhihui"); // 此为测试数据，实际使用时填入指定要连接的server的apk包名
        mVoiceClient = VoiceClient.instance();

    }
    public void start(){
        mVoiceClient.setListener(new VoiceClient.ConnectionListener() {
            @Override
            public void onDisconnected(int arg0) {
                Log.d(TAG, "onDisconnected, code=" + arg0);
                isConnected = false;
//                if (mAiqiyiSpeakTextListener != null) {
//                    mAiqiyiSpeakTextListener.speakText("爱奇艺服务连接失败，错误码" + arg0);
//                }
            }

            @Override
            public void onConnected() {
                Log.d(TAG, "onConnected");
                isConnected = true;
            }

        });
        mVoiceClient.connect();
    }

    public void parseOrder(String order){
        if (!isConnected){
            mAiqiyiSpeakTextListener.speakText("爱奇艺服务未连接，正在重连");
            start();
            return;
        }
        if (order.startsWith("搜索")){
            String movName = order.substring(order.indexOf("搜索") + 2, order.length());
            if (TextUtils.isEmpty(movName)){
                return;
            }
            searchVideo(movName);
        }
        if (order.startsWith("显示")){
            String channelName = order.substring(order.indexOf("显示") + 2, order.length());
            if (TextUtils.isEmpty(channelName)){
                return;
            }else {
                if (textInChannel(channelName)) {
                    controlVideo(channelName);
                }
            }
        }
        if (textInControlMode(order)){
            controlVideo(order);
        }
        if (textInKeyCodes(order) >= 0){
            simulateKeyEvent(textInKeyCodes(order));
        }
        if (order.equals("上一集")||order.equals("上一级")){
            playPre();
        }
        if (order.equals("下一集")||order.equals("下一级")){
            playNext();
        }
        if (order.startsWith("播放")){
            String order1 = order.substring(2, order.length());
            if (order1.startsWith("电影")){
                String order2 = order.substring(2, order.length());
                if(TextUtils.isEmpty(order2)){
                    mAiqiyiSpeakTextListener.speakText("要加上电影名字哦");
                }else {
                    playEventForFilm(order2);
                }
            }else if (order1.startsWith("电视剧")){
                String order2 = order.substring(3, order.length());
                if(TextUtils.isEmpty(order2)){
                    mAiqiyiSpeakTextListener.speakText("要加上电视剧名字哦");
                }else {
                    playEventForVideo(order2,1);
                }
            }
        }
        if (order.startsWith("跳转到")){
            String order1 = order.substring(3,order.length());
            int time = SwitchTimeNumberUtil.getNumerFromTimeText2(order1+" ");
            if (time >= 0){
                seek_to(time*1000);
            }
        }
        if (order.startsWith("前进")||order.startsWith("快进")){
            String order1 = order.substring(2,order.length());
            int time = SwitchTimeNumberUtil.getNumerFromTimeText2(order1+" ");
            if (time >= 0){
                seek_offset(time*1000);
            }
        }
        if (order.startsWith("后退")||order.startsWith("快退")){
            String order1 = order.substring(2,order.length());
            int time = SwitchTimeNumberUtil.getNumerFromTimeText2(order1+" ");
            if (time >= 0){
                seek_offset(time*-1000);
            }
        }

    }

    private int textInKeyCodes(String order) {
        String[] keyCodesName = {"向左","向右","向上","向下"};
        for (int i = 0 ;i<keyCodesName.length;i++){
            if (order.equals(keyCodesName[i])){
                return i;
            }
        }
        return -1;
    }

    private String[] controlMode = {"播放","暂停","退出","全屏播放","向上","向下","向左","向右"};
    private boolean textInControlMode(String str){
        for (int i = 0; i < controlMode.length; i++){
            if (str.equals(controlMode[i])){
                return true;
            }
        }
        return false;
    }

    String[] channel = new String[] { "电视剧", "电影", "动漫", "综艺", "少儿", "娱乐", "音乐", "旅游", "纪录片", "搞笑", "教育", "资讯",
            "财经", "体育", "军事", "片花", "汽车", "时尚", "母婴", "脱口秀", "科技", "最近更新" };
    private boolean textInChannel(String str){
        for (int i = 0; i < channel.length; i++){
            if (str.equals(channel[i])){
                return true;
            }
        }
        return false;
    }

    /***************************************
     *
     *  爱奇艺控制各个事件
     *
     **************************************/
    private void searchVideo(String videoName) {
        VoiceEvent voiceEvent = VoiceEventFactory.createSearchEvent(videoName);
        Log.d(TAG, "searchVideo() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "searchVideo() result =  " + handled);
    }

    private void controlVideo(String controllMode) {
        VoiceEvent voiceEvent = VoiceEventFactory.createKeywordsEvent(controllMode);
        Log.d(TAG, "controlVideo() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "controlVideo() result =  " + handled);
    }
    private void playEventForFilm(String filmName) {
        VoiceEvent voiceEvent = VoiceEventFactory.createPlayEvent(filmName).setChannelName("电影");
        Log.d(TAG, "testPlayEvent() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testPlayEvent() result =  " + handled);
    }

    private void simulateKeyEvent(int keycodeIndex) {
        int[] keycodes = new int[] { KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_PAGE_UP,
                KeyEvent.KEYCODE_PAGE_DOWN };
        VoiceEvent voiceEvent = VoiceEventFactory.createKeyEvent(keycodes[keycodeIndex]);
        Log.d(TAG, "testSearch() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testSearch() result =  " + handled + ", keycode=" + keycodes[keycodeIndex]);
    }

    // 调起某个电视剧的某一集的 播放页面
    private void playEventForVideo(String videoName,int index) {
        VoiceEvent voiceEvent = VoiceEventFactory.createPlayEvent(videoName).setChannelName("电视剧").setEpisodeIndex(index);
        Log.d(TAG, "testPlayEvent() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testPlayEvent() result =  " + handled);
    }
    private void playPre() {
        VoiceEvent voiceEvent = VoiceEventFactory.createPreviousEpisodeEvent();
        Log.d(TAG, "testNext() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testNext() result =  " + handled);
    }

    private void playNext() {
        VoiceEvent voiceEvent = VoiceEventFactory.createNextEpisodeEvent();
        Log.d(TAG, "testNext() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testNext() result =  " + handled);
    }
    private void seek_to(int time) {
        VoiceEvent voiceEvent = VoiceEventFactory.createSeekToEvent(time);
        Log.d(TAG, "testSeek_to() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testSeek_to() result =  " + handled);
    }
    private void seek_offset(int time) {
        VoiceEvent voiceEvent = VoiceEventFactory.createSeekOffsetEvent(time);
        Log.d(TAG, "testSeek_offset() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testSeek_offset() result =  " + handled);
    }
    private void testKeyWord() {
        String[] keywords = new String[] { "电视剧", "电影", "动漫", "综艺", "少儿", "娱乐", "音乐", "旅游", "纪录片", "搞笑", "教育", "资讯",
                "财经", "体育", "军事", "片花", "汽车", "时尚", "母婴", "脱口秀", "科技", "最近更新" };
        for (int i = 0; i < keywords.length; i++) {
            VoiceEvent voiceEvent = VoiceEventFactory.createKeywordsEvent(keywords[i]);// "影视""电视""游戏""应用""设置"
            Log.d(TAG, "testKeyWord() event = " + voiceEvent);
            boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
            Log.d(TAG, "testKeyWord() result =  " + handled);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void testKeyEvent() {
        int[] keycodes = new int[] { KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_PAGE_UP,
                KeyEvent.KEYCODE_PAGE_DOWN };
        for (int keycode : keycodes) {
            VoiceEvent voiceEvent = VoiceEventFactory.createKeyEvent(keycode);
            Log.d(TAG, "testSearch() event = " + voiceEvent);
            boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
            Log.d(TAG, "testSearch() result =  " + handled + ", keycode=" + keycode);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void testPlay() {
        VoiceEvent voiceEvent = VoiceEventFactory.createKeywordsEvent("播放");
        Log.d(TAG, "testPlay() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testPlay() result =  " + handled);
    }

    private void testPause() {
        VoiceEvent voiceEvent = VoiceEventFactory.createKeywordsEvent("暂停");
        Log.d(TAG, "testPause() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testPause() result =  " + handled);
    }

    private void testSeek_to() {
        VoiceEvent voiceEvent = VoiceEventFactory.createSeekToEvent(600000);
        Log.d(TAG, "testSeek_to() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testSeek_to() result =  " + handled);
    }

    private void testSeek_offset() {
        VoiceEvent voiceEvent = VoiceEventFactory.createSeekOffsetEvent(-100000);
        Log.d(TAG, "testSeek_offset() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testSeek_offset() result =  " + handled);
    }

    private void testPre() {
        VoiceEvent voiceEvent = VoiceEventFactory.createPreviousEpisodeEvent();
        Log.d(TAG, "testNext() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testNext() result =  " + handled);
    }

    private void testNext() {
        VoiceEvent voiceEvent = VoiceEventFactory.createNextEpisodeEvent();
        Log.d(TAG, "testNext() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testNext() result =  " + handled);
    }

    private void testSelect(int index) {
        VoiceEvent voiceEvent = VoiceEventFactory.createSelectEpisodeIndexEvent(index);
        Log.d(TAG, "testSelect() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testSelect() result =  " + handled);
    }

    // 调起某个电视剧的某一集的 播放页面
    private void testPlayEvent() {
        VoiceEvent voiceEvent = VoiceEventFactory.createPlayEvent("锦绣未央").setChannelName("电视剧").setEpisodeIndex(2);
        Log.d(TAG, "testPlayEvent() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testPlayEvent() result =  " + handled);
    }

    // 调起某个电影 的 播放页面
    private void testPlayEventForFilm() {
        VoiceEvent voiceEvent = VoiceEventFactory.createPlayEvent("战狼").setChannelName("电影");
        Log.d(TAG, "testPlayEvent() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testPlayEvent() result =  " + handled);
    }

    // 调起全屏播放页面（6.4 和 7.5及以后版本支持）
    private void testPlayFullscreen() {
        VoiceEvent voiceEvent = VoiceEventFactory.createKeywordsEvent("全屏播放");
        Log.d(TAG, "testPlayFullscreen() event = " + voiceEvent);
        boolean handled = mVoiceClient.dispatchVoiceEvent(voiceEvent);
        Log.d(TAG, "testPlayFullscreen() result =  " + handled);
    }
}
