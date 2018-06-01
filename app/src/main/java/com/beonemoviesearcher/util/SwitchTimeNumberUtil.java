package com.beonemoviesearcher.util;

/**
 * Created by wangfan on 2018/5/17.
 */

public class SwitchTimeNumberUtil {

    public static final String MM ="零 一 二 三 四 五 六 七 八 九 十 十一 十二 十三 十四 十五 十六 十七 十八 十九 二十" +
            " 二十一 二十二 二十三 二十四 二十五 二十六 二十七 二十八 二十九 三十" +
            " 三十一 三十二 三十三 三十四 三十五 三十六 三十七 三十八 三十九 四十" +
            " 四十一 四十二 四十三 四十四 四十五 四十六 四十七 四十八 四十九 五十" +
            " 五十一 五十二 五十三 五十四 五十五 五十六 五十七 五十八 五十九 六十";
    public static String[] MM_ARRAY = MM.split(" ");
    public static int getNumerFromTimeText2(String timeText){
        int time = 0;
        String tt = timeText.replace(" ","").replace("小","").replace("钟","");
        if (!tt.contains("时")){
            tt = "时"+tt;
        }
        if (!tt.contains("秒")){
            tt = tt+"秒";
        }
        if (!tt.contains("分")){
            String[] ttt = tt.split("时");
            tt = ttt[0]+"时分"+ttt[1];
        }
        //处理小时数
        String hTime = tt.substring(0,tt.indexOf("时"));
        if (isNumeric(hTime)){
            int hhTime = Integer.parseInt(hTime);
            if (hhTime == 1){
                time += 3600;
            }else if(hhTime == 2){
                time += 7200;
            }else {
                return -1;
            }
        }else if (hTime.equals("")){

        }else {
            if (hTime.equals("一")){
                time += 3600;
            }else if (hTime.equals("二")||hTime.equals("两")){
                time += 7200;
            }else
                return -1;
        }
        //处理分钟数
        String mTime = tt.substring(tt.indexOf("时")+1,tt.indexOf("分"));
        if (mTime.equals("")){
            time += 0;
        }else {
            if (isNumeric(mTime)) {
                int mmTime = Integer.parseInt(mTime);
                if (mmTime >= 0 && mmTime <= 60) {
                    time += mmTime * 60;
                }
            }else {
                int mmTime = textToNumber(mTime);
                if (mmTime == -1) {
                    return -1;
                } else {
                    time += mmTime * 60;
                }
            }
        }
        //处理秒数
        String sTime = tt.substring(tt.indexOf("分")+1,tt.indexOf("秒"));
        if (isNumeric(sTime)){
            int ssTime = Integer.parseInt(sTime);
            if(ssTime >= 0 && ssTime <= 60){
                time += ssTime;
            }
        }else if (sTime.equals("")){

        }else {
            int ssTime = textToNumber(sTime);
            if (ssTime == -1){
                return -1;
            }else {
                time += ssTime;
            }
        }

        return time;
    }

    public static boolean isNumeric(String str) {
        try{
            Integer.parseInt(str);
            return true;
        }catch(NumberFormatException e) {
            return false;
        }
    }
    public static int textToNumber(String text){
        int i = MM_ARRAY.length-1;
        for (;i>=0;i--){
            if (text.equals(MM_ARRAY[i])){
                break;
            }
        }
        return i;
    }

    public static int getNumerFromTimeText(String timeText){
        int time = 0;
        String tt = timeText.replace(" ","").replace("小","").replace("钟","");
        if (!tt.contains("时")){
            tt = "时"+tt;
        }
        if (!tt.contains("分")){
            String[] ttt = tt.split("时");
            tt = ttt[0]+"时分"+ttt[1];
        }
        String[] hTime = tt.split("时");
        String[] mTime;
        String[] sTime;
        if(hTime.length == 2){
            mTime = hTime[1].split("分");
            if (hTime[0].equals("一")){
                time += 3600;
            }else if(hTime[0].equals("二")){
                time += 7200;
            }else if (hTime[0].equals("")){
                time += 0;
            }else  {
                return -1;
            }
        }else if (hTime.length == 1){
            if (hTime[0].equals("一")){
                time += 3600;
            }else if(hTime[0].equals("二")){
                time += 7200;
            }else {
                return -1;
            }
            return time;
        }else {
            return -1;
        }
        if (mTime.length == 2){
            sTime = mTime[1].split("秒");
            int i=0;
            for (;i<MM_ARRAY.length;i++){
                if (mTime[0].equals(MM_ARRAY[i])){
                    break;
                }
            }
            if (i < MM_ARRAY.length){
                time += (i+1)*60;
            }else if (i == 60 && mTime[0].equals("")) {

            }else {
                return -1;
            }
        }else if (mTime.length == 1){
            int i=0;
            for (;i<MM_ARRAY.length;i++){
                if (mTime[0].equals(MM_ARRAY[i])){
                    break;
                }
            }
            if (i < MM_ARRAY.length){
                time += (i+1)*60;
            }else {
                return -1;
            }
            return time;
        }else {
            return -1;
        }
        if (sTime.length == 1){
            int i=0;
            for (;i<MM_ARRAY.length;i++){
                if (sTime[0].equals(MM_ARRAY[i])){
                    break;
                }
            }
            if (i < MM_ARRAY.length){
                time += i+1;
            }else {
                return -1;
            }
            return time;
        }else {
            return -1;
        }
    }
}
