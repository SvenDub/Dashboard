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

package nl.svendubbeld.car.obd;

public class Car {

    private String mVin = "";
    private String mName = "";

    private float[] mGears = {
            0f,
            0f,
            0f,
            0f,
            0f,
            0f,
            0f,
            0f
    };

    public Car(String vin) {
        init(vin, "", new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f});
    }

    public Car(String vin, String name) {
        init(vin, name, new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f});
    }

    public Car(String vin, String name, float[] gears) {
        init(vin, name, gears);
    }

    private void init(String vin, String name, float[] gears) {
        mVin = vin;
        mName = name;
        mGears = gears;
    }

    public String getVin() {
        return mVin;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public float[] getGears() {
        return mGears;
    }

    public void setGears(float[] gears) {
        mGears = gears;
    }
}
