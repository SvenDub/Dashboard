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
import android.widget.TextView;

import java.util.ArrayList;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.database.DatabaseHandler;

public class NavigationFavoritesAdapter extends ArrayAdapter<NavigationFavoritesAdapter.NavigationFavorite> {

    Context mContext;
    DatabaseHandler mDb;

    ArrayList<NavigationFavorite> mFavorites = new ArrayList<>();

    int mResource;

    public NavigationFavoritesAdapter(Context context, int resource) {
        super(context, resource);

        mContext = context;
        mResource = resource;

        mDb = new DatabaseHandler(mContext);

        loadFromDatabase();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(mResource, parent, false);

        TextView name = (TextView) v.findViewById(R.id.name);
        TextView address = (TextView) v.findViewById(R.id.address);

        name.setText(getItem(position).getName());
        address.setText(getItem(position).getAddress());

        return v;
    }

    @Override
    public NavigationFavorite getItem(int position) {
        return mFavorites.get(position);
    }

    @Override
    public int getCount() {
        return mFavorites.size();
    }

    @Override
    public void add(NavigationFavorite object) {
        mFavorites.add(object);
    }

    public void edit(int position, String name, String address) {
        getItem(position).setName(name);
        getItem(position).setAddress(address);
    }

    public void remove(int position) {
        mFavorites.remove(position);
    }

    public void saveToDatabase() {
        mDb.setNavigationFavorites(mFavorites);
    }

    public void loadFromDatabase() {
        mFavorites = mDb.getNavigationFavorites();
        notifyDataSetChanged();
    }

    public void setResource(int resource) {
        mResource = resource;
    }

    public static class NavigationFavorite {

        String mName;
        String mAddress;

        public NavigationFavorite(String name, String address) {
            setName(name);
            setAddress(address);
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public String getAddress() {
            return mAddress;
        }

        public void setAddress(String address) {
            mAddress = address;
        }
    }

}
