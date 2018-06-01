package com.wangfan.testbeoneaidserviceapplication;

import com.beonemoviesearcher.util.SwitchTimeNumberUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
//        String s = "时23";
//        String[] a = s.split("时");
//        for (String b:a){
//            System.out.printf(b+"\n");
//        }
        String tt = "一小时";
//        String hTime = tt.substring(0,tt.indexOf("时"));
//        String mTime = tt.substring(tt.indexOf("时")+1,tt.indexOf("分"));
//        String sTime = tt.substring(tt.indexOf("分")+1,tt.indexOf("秒"));
//        System.out.printf(hTime);
//        System.out.printf(mTime);
//        System.out.printf(sTime);
        System.out.printf(SwitchTimeNumberUtil.getNumerFromTimeText2(tt)+"");
    }

}