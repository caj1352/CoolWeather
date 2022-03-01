package com.example.myapplication;

import org.junit.Test;

import static org.junit.Assert.*;

import com.caj1352.coolweather.util.HFUtil;

import java.text.ParseException;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void date_isCorrect() throws ParseException {
        String updateTime = HFUtil.formatDateString("yyyy-MM-dd HH:mm", "2020-06-30T22:00+08:00");
        System.out.println(updateTime);
    }
}