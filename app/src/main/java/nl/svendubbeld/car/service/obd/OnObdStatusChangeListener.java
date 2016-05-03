package nl.svendubbeld.car.service.obd;

public interface OnObdStatusChangeListener {
    void onObdStatusChanged(ObdService.Status status);
}
