package nl.svendubbeld.car.unit.speed;

public class KilometerPerHour extends Speed {

    public KilometerPerHour() {
    }

    public KilometerPerHour(double value) {
        super(value);
    }

    public KilometerPerHour(Speed other) {
        super(other);
    }

    @Override
    public String getUnit() {
        return "km/h";
    }

    @Override
    protected double rawToValue(double raw) {
        return raw * 3.6;
    }

    @Override
    protected double valueToRaw(double value) {
        return value / 3.6;
    }
}
