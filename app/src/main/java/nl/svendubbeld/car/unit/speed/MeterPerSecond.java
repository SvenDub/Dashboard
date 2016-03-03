package nl.svendubbeld.car.unit.speed;

public class MeterPerSecond extends Speed {

    public MeterPerSecond() {
    }

    public MeterPerSecond(double value) {
        super(value);
    }

    public MeterPerSecond(Speed other) {
        super(other);
    }

    @Override
    public String getUnit() {
        return "m/s";
    }

    @Override
    protected double rawToValue(double raw) {
        return raw;
    }

    @Override
    protected double valueToRaw(double value) {
        return value;
    }
}
