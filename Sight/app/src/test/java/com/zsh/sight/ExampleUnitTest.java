package com.zsh.sight;

import org.junit.Test;

import static org.junit.Assert.*;

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

    // 判断账户是否存在
//    public static boolean exist_account(String account){

    @Test
    public void test(){
        LoginServer.exist_account("111");
    }

}