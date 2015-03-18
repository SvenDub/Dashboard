/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sven Dubbeld
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.svendubbeld.car;

public class Log {

    private static boolean LOG_D = BuildConfig.LOG_D;
    private static boolean LOG_E = BuildConfig.LOG_E;
    private static boolean LOG_I = BuildConfig.LOG_I;
    private static boolean LOG_V = BuildConfig.LOG_V;
    private static boolean LOG_W = BuildConfig.LOG_W;
    private static boolean LOG_WTF = BuildConfig.LOG_WTF;

    public static void d(String tag, String msg) {
        if (LOG_D)
            android.util.Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (LOG_D)
            android.util.Log.d(tag, msg, tr);
    }

    public static void e(String tag, String msg) {
        if (LOG_E)
            android.util.Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (LOG_E)
            android.util.Log.e(tag, msg, tr);
    }

    public static void i(String tag, String msg) {
        if (LOG_I)
            android.util.Log.i(tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (LOG_I)
            android.util.Log.i(tag, msg, tr);
    }

    public static void v(String tag, String msg) {
        if (LOG_V)
            android.util.Log.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (LOG_V)
            android.util.Log.v(tag, msg, tr);
    }

    public static void w(String tag, String msg) {
        if (LOG_W)
            android.util.Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (LOG_W)
            android.util.Log.w(tag, msg, tr);
    }

    public static void w(String tag, Throwable tr) {
        if (LOG_W)
            android.util.Log.w(tag, tr);
    }

    public static void wtf(String tag, String msg) {
        if (LOG_WTF)
            android.util.Log.wtf(tag, msg);
    }

    public static void wtf(String tag, String msg, Throwable tr) {
        if (LOG_WTF)
            android.util.Log.wtf(tag, msg, tr);
    }

    public static void wtf(String tag, Throwable tr) {
        if (LOG_WTF)
            android.util.Log.wtf(tag, tr);
    }
}