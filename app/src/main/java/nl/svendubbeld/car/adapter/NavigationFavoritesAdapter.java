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

/**
 * ArrayAdapter that holds NavigationFavorites.
 */
public class NavigationFavoritesAdapter extends ArrayAdapter<NavigationFavoritesAdapter.NavigationFavorite> {

    private Context mContext;
    private DatabaseHandler mDb;

    private ArrayList<NavigationFavorite> mFavorites = new ArrayList<>();

    private int mResource;

    /**
     * Create a new Adapter.
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file to use when instantiating views.
     */
    public NavigationFavoritesAdapter(Context context, int resource) {
        super(context, resource);

        mContext = context;
        mResource = resource;

        mDb = new DatabaseHandler(mContext);

        loadFromDatabase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView != null ? convertView : LayoutInflater.from(mContext).inflate(mResource, parent, false);

        TextView name = (TextView) v.findViewById(R.id.name);
        TextView address = (TextView) v.findViewById(R.id.address);

        name.setText(getItem(position).getName());
        address.setText(getItem(position).getAddress());

        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigationFavorite getItem(int position) {
        return mFavorites.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mFavorites.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(NavigationFavorite object) {
        mFavorites.add(object);
    }

    /**
     * Modifies an item in the adapter.
     *
     * @param position The position of the item.
     * @param name     The new name of the {@link nl.svendubbeld.car.adapter.NavigationFavoritesAdapter.NavigationFavorite}.
     * @param address  The new address of the {@link nl.svendubbeld.car.adapter.NavigationFavoritesAdapter.NavigationFavorite}.
     */
    public void edit(int position, String name, String address) {
        getItem(position).setName(name);
        getItem(position).setAddress(address);
    }

    /**
     * Removes an item from the adapter.
     *
     * @param position The position of the item.
     */
    public void remove(int position) {
        mFavorites.remove(position);
    }

    /**
     * Saves the current contents of the adapter to database.
     */
    public void saveToDatabase() {
        mDb.setNavigationFavorites(mFavorites);
    }

    /**
     * Loads the list of {@link nl.svendubbeld.car.adapter.NavigationFavoritesAdapter.NavigationFavorite}
     * from the database.
     */
    public void loadFromDatabase() {
        mFavorites = mDb.getNavigationFavorites();
        notifyDataSetChanged();
    }

    /**
     * An object containing data about a favorite for navigation.
     */
    public static class NavigationFavorite {

        private String mName;
        private String mAddress;

        /**
         * Create a new favorite.
         *
         * @param name    The name of the favorite.
         * @param address The address of the favorite.
         */
        public NavigationFavorite(String name, String address) {
            setName(name);
            setAddress(address);
        }

        /**
         * @return The name of the favorite.
         */
        public String getName() {
            return mName;
        }

        /**
         * @param name The name of the favorite.
         */
        public void setName(String name) {
            mName = name;
        }

        /**
         * @return The address of the favorite.
         */
        public String getAddress() {
            return mAddress;
        }

        /**
         * @param address The address of the favorite.
         */
        public void setAddress(String address) {
            mAddress = address;
        }
    }

}
