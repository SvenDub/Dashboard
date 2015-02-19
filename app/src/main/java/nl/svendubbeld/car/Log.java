package nl.svendubbeld.car;

public class Log {

    private boolean LOG_D = true;
    private boolean LOG_E = true;
    private boolean LOG_I = true;
    private boolean LOG_V = false;
    private boolean LOG_W = true;
    private boolean LOG_WTF = true;

    public void d(String tag, String msg) {
        if (LOG_D)
            android.util.Log.d(tag, msg);
    }

    public void d(String tag, String msg, Throwable tr) {
        if (LOG_D)
            android.util.Log.d(tag, msg, tr);
    }

    public void e(String tag, String msg) {
        if (LOG_E)
            android.util.Log.e(tag, msg);
    }

    public void e(String tag, String msg, Throwable tr) {
        if (LOG_E)
            android.util.Log.e(tag, msg, tr);
    }

    public void i(String tag, String msg) {
        if (LOG_I)
            android.util.Log.i(tag, msg);
    }

    public void i(String tag, String msg, Throwable tr) {
        if (LOG_I)
            android.util.Log.i(tag, msg, tr);
    }

    public boolean isLOG_D() {
        return this.LOG_D;
    }

    public boolean isLOG_E() {
        return this.LOG_E;
    }

    public boolean isLOG_I() {
        return this.LOG_I;
    }

    public boolean isLOG_V() {
        return this.LOG_V;
    }

    public boolean isLOG_W() {
        return this.LOG_W;
    }

    public boolean isLOG_WTF() {
        return this.LOG_WTF;
    }

    public void setLOG_D(boolean LOG_D) {
        this.LOG_D = LOG_D;
    }

    public void setLOG_E(boolean LOG_E) {
        this.LOG_E = LOG_E;
    }

    public void setLOG_I(boolean LOG_I) {
        this.LOG_I = LOG_I;
    }

    public void setLOG_V(boolean LOG_V) {
        this.LOG_V = LOG_V;
    }

    public void setLOG_W(boolean LOG_W) {
        this.LOG_W = LOG_W;
    }

    public void setLOG_WTF(boolean LOG_WTF) {
        this.LOG_WTF = LOG_WTF;
    }

    public void v(String tag, String msg) {
        if (LOG_V)
            android.util.Log.v(tag, msg);
    }

    public void v(String tag, String msg, Throwable tr) {
        if (LOG_V)
            android.util.Log.v(tag, msg, tr);
    }

    public void w(String tag, String msg) {
        if (LOG_W)
            android.util.Log.w(tag, msg);
    }

    public void w(String tag, String msg, Throwable tr) {
        if (LOG_W)
            android.util.Log.w(tag, msg, tr);
    }

    public void w(String tag, Throwable tr) {
        if (LOG_W)
            android.util.Log.w(tag, tr);
    }

    public void wtf(String tag, String msg) {
        if (LOG_WTF)
            android.util.Log.wtf(tag, msg);
    }

    public void wtf(String tag, String msg, Throwable tr) {
        if (LOG_WTF)
            android.util.Log.wtf(tag, msg, tr);
    }

    public void wtf(String tag, Throwable tr) {
        if (LOG_WTF)
            android.util.Log.wtf(tag, tr);
    }
}