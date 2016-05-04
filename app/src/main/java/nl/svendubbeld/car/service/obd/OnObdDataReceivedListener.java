package nl.svendubbeld.car.service.obd;

public interface OnObdDataReceivedListener {
    void onObdDataReceived(ObdService.Data dataType, Object data);
}
