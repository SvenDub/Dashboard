package nl.svendubbeld.car.unit.speed;

public class MilesPerHour extends Speed {

    public MilesPerHour() {
    }

    public MilesPerHour(double value) {
        super(value);
    }

    public MilesPerHour(Speed other) {
        super(other);
    }

    @Override
    public String getUnit() {
        return "mph";
    }

    @Override
    protected double rawToValue(double raw) {
        return raw/(1397d/3125d);
    }

    @Override
    protected double valueToRaw(double value) {
        return value*(1397d/3125d);
    }
}
