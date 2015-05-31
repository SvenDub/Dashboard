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

package nl.svendubbeld.car.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.database.DatabaseHandler;
import nl.svendubbeld.car.obd.Car;

/**
 * ArrayAdapter that holds {@link Car}.
 */
public class CarAdapter extends ArrayAdapter<Car> {

    private final Object mLock = new Object();
    private Context mContext;
    private DatabaseHandler mDb;

    private ArrayList<Car> mCars = new ArrayList<>();
    private ArrayList<Car> mOriginalValues;

    private int mResource;

    private Filter mFilter;

    /**
     * Create a new Adapter.
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file to use when instantiating views.
     */
    public CarAdapter(Context context, int resource) {
        super(context, resource);

        mContext = context;
        mResource = resource;

        mDb = new DatabaseHandler(mContext);

        loadFromDatabase();

        mFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (mOriginalValues == null) {
                    synchronized (mLock) {
                        mOriginalValues = new ArrayList<>(mCars);
                    }
                }

                if (constraint == null || constraint.length() == 0) {
                    ArrayList<Car> list;
                    synchronized (mLock) {
                        list = new ArrayList<>(mOriginalValues);
                    }
                    results.values = list;
                    results.count = list.size();
                } else {
                    String constraintString = constraint.toString().toLowerCase();

                    ArrayList<Car> values;
                    synchronized (mLock) {
                        values = new ArrayList<>(mOriginalValues);
                    }

                    ArrayList<Car> newValues = new ArrayList<>();

                    for (Car car : values) {
                        if (car.getName().toLowerCase().contains(constraintString) || car.getVin().toLowerCase().contains(constraintString)) {
                            newValues.add(car);
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof Car) {
                    return ((Car) resultValue).getVin();
                } else {
                    return "";
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mCars = (ArrayList<Car>) results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView != null ? convertView : LayoutInflater.from(mContext).inflate(mResource, parent, false);

        TextView vin = (TextView) v.findViewById(R.id.vin);
        TextView name = (TextView) v.findViewById(R.id.name);

        Car item = getItem(position);

        name.setText(item.getName());
        vin.setText(item.getVin());

        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Car getItem(int position) {
        return mCars.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mCars.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Car object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mCars.add(object);
            }
        }
    }

    /**
     * Removes an item from the adapter.
     *
     * @param position The position of the item.
     */
    public void remove(int position) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(position);
            } else {
                mCars.remove(position);
            }
        }
    }

    /**
     * Saves the current contents of the adapter to database.
     */
    public void saveToDatabase() {
        mDb.setCars(mCars);
    }

    /**
     * Loads the list of {@link Car} from the database.
     */
    public void loadFromDatabase() {
        mCars.clear();

        try {
            mCars.addAll(mDb.getCars());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

}
