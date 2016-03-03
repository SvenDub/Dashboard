package nl.svendubbeld.car.unit.speed;

import nl.svendubbeld.car.unit.Unit;

public abstract class Speed extends Unit {

    public Speed() {
    }

    public Speed(double value) {
        super(value);
    }

    public Speed(Speed other) {
        setRawValue(other.getRawValue());
    }
}
