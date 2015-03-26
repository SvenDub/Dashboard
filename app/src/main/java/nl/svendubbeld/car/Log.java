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

/**
 * <p>API for logging.</p> <p> Works the same as {@link android.util.Log} but only outputs a log
 * level if it is enabled in the BuildConfig. </p>
 */
public final class Log {

    /**
     * Log on {@link android.util.Log#DEBUG DEBUG} level.
     */
    private final static boolean LOG_D = BuildConfig.LOG_D;
    /**
     * Log on {@link android.util.Log#ERROR ERROR} level.
     */
    private final static boolean LOG_E = BuildConfig.LOG_E;
    /**
     * Log on {@link android.util.Log#INFO INFO} level.
     */
    private final static boolean LOG_I = BuildConfig.LOG_I;
    /**
     * Log on {@link android.util.Log#VERBOSE VERBOSE} level.
     */
    private final static boolean LOG_V = BuildConfig.LOG_V;
    /**
     * Log on {@link android.util.Log#WARN WARN} level.
     */
    private final static boolean LOG_W = BuildConfig.LOG_W;
    /**
     * Log on "What a Terrible Failure" ({@link android.util.Log#ASSERT ASSERT}) level.
     */
    private final static boolean LOG_WTF = BuildConfig.LOG_WTF;

    /**
     * Send a {@link android.util.Log#DEBUG DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void d(String tag, String msg) {
        if (LOG_D)
            android.util.Log.d(tag, msg);
    }

    /**
     * Send a {@link android.util.Log#DEBUG DEBUG} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static void d(String tag, String msg, Throwable tr) {
        if (LOG_D)
            android.util.Log.d(tag, msg, tr);
    }

    /**
     * Send an {@link android.util.Log#ERROR ERROR} log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void e(String tag, String msg) {
        if (LOG_E)
            android.util.Log.e(tag, msg);
    }

    /**
     * Send an {@link android.util.Log#ERROR ERROR} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static void e(String tag, String msg, Throwable tr) {
        if (LOG_E)
            android.util.Log.e(tag, msg, tr);
    }

    /**
     * Send an {@link android.util.Log#INFO INFO} log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void i(String tag, String msg) {
        if (LOG_I)
            android.util.Log.i(tag, msg);
    }

    /**
     * Send an {@link android.util.Log#INFO INFO} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static void i(String tag, String msg, Throwable tr) {
        if (LOG_I)
            android.util.Log.i(tag, msg, tr);
    }

    /**
     * Send a {@link android.util.Log#VERBOSE VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void v(String tag, String msg) {
        if (LOG_V)
            android.util.Log.v(tag, msg);
    }

    /**
     * Send a {@link android.util.Log#VERBOSE VERBOSE} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static void v(String tag, String msg, Throwable tr) {
        if (LOG_V)
            android.util.Log.v(tag, msg, tr);
    }

    /**
     * Send a {@link android.util.Log#WARN WARN} log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void w(String tag, String msg) {
        if (LOG_W)
            android.util.Log.w(tag, msg);
    }

    /**
     * Send a {@link android.util.Log#WARN WARN} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static void w(String tag, String msg, Throwable tr) {
        if (LOG_W)
            android.util.Log.w(tag, msg, tr);
    }

    /**
     * Send a {@link android.util.Log#WARN WARN} exception.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param tr  An exception to log
     */
    public static void w(String tag, Throwable tr) {
        if (LOG_W)
            android.util.Log.w(tag, tr);
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen. The error will always
     * be logged at level {@link android.util.Log#ASSERT ASSERT} with the call stack. Depending on
     * system configuration, a report may be added to the {@link android.os.DropBoxManager
     * DropBoxManager} and/or the process may be terminated immediately with an error dialog.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void wtf(String tag, String msg) {
        if (LOG_WTF)
            android.util.Log.wtf(tag, msg);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen. Similar to {@link
     * #wtf(String, Throwable)}, with a message as well.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static void wtf(String tag, String msg, Throwable tr) {
        if (LOG_WTF)
            android.util.Log.wtf(tag, msg, tr);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen. Similar to {@link
     * #wtf(String, String)}, with an exception to log.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param tr  An exception to log
     */
    public static void wtf(String tag, Throwable tr) {
        if (LOG_WTF)
            android.util.Log.wtf(tag, tr);
    }
}