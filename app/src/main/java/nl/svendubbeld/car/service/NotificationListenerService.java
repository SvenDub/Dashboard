package nl.svendubbeld.car.service;

public class NotificationListenerService extends android.service.notification.NotificationListenerService {

    private static NotificationListenerService mService = null;

    public static NotificationListenerService getService() {
        return mService;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mService = this;
    }
}
