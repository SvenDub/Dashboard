package nl.svendubbeld.car.unit;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

public abstract class Unit implements Comparable<Unit> {

    private BigDecimal mRawValue;

    public Unit() {

    }

    public Unit(double value) {
        setValue(value);
    }

    public double getRawValue() {
        return mRawValue.doubleValue();
    }

    public void setRawValue(double rawValue) {
        mRawValue = new BigDecimal(rawValue);
    }

    public double getValue() {
        return rawToValue(getRawValue());
    };

    public void setValue(double value){
        setRawValue(valueToRaw(value));
    }

    public String getFormattedString(int decimals) {
        return getValueString(decimals) + getUnit();
    }

    public String getFormattedString() {
        return getFormattedString(2);
    }

    public String getValueString(int decimals) {
        BigDecimal bigDecimal = new BigDecimal(getValue());
        bigDecimal = bigDecimal.setScale(decimals, BigDecimal.ROUND_HALF_UP);
        return bigDecimal.toEngineeringString();
    }

    public String getValueString() {
        return getValueString(2);
    }

    public abstract String getUnit();

    protected abstract double rawToValue(double raw);

    protected abstract double valueToRaw(double value);

    @Override
    public int compareTo(@NonNull Unit another) {
        return mRawValue.compareTo(another.mRawValue);
    }
}
