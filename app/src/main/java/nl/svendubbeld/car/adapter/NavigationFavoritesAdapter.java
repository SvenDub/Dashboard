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
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.database.DatabaseHandler;
import nl.svendubbeld.car.preference.Preferences;

/**
 * ArrayAdapter that holds NavigationFavorites.
 */
public class NavigationFavoritesAdapter extends ArrayAdapter<NavigationFavoritesAdapter.NavigationFavorite> {

    private final Object mLock = new Object();
    private Context mContext;
    private DatabaseHandler mDb;

    private ArrayList<NavigationFavorite> mFavorites = new ArrayList<>();
    private ArrayList<NavigationFavorite> mOriginalValues;

    private boolean mIncludeContacts;

    private int mResource;

    private Filter mFilter;

    /**
     * Create a new Adapter.
     *
     * @param context         The current context.
     * @param resource        The resource ID for a layout file to use when instantiating views.
     * @param includeContacts Whether to include contacts in the list.
     */
    public NavigationFavoritesAdapter(Context context, int resource, boolean includeContacts) {
        super(context, resource);

        mContext = context;
        mResource = resource;

        mDb = new DatabaseHandler(mContext);

        mIncludeContacts = includeContacts;

        loadFromDatabase();

        mFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (mOriginalValues == null) {
                    synchronized (mLock) {
                        mOriginalValues = new ArrayList<>(mFavorites);
                    }
                }

                if (constraint == null || constraint.length() == 0) {
                    ArrayList<NavigationFavorite> list;
                    synchronized (mLock) {
                        list = new ArrayList<>(mOriginalValues);
                    }
                    results.values = list;
                    results.count = list.size();
                } else {
                    String constraintString = constraint.toString().toLowerCase();

                    ArrayList<NavigationFavorite> values;
                    synchronized (mLock) {
                        values = new ArrayList<>(mOriginalValues);
                    }

                    ArrayList<NavigationFavorite> newValues = new ArrayList<>();

                    for (NavigationFavorite favorite : values) {
                        if (favorite.getName().toLowerCase().contains(constraintString) || favorite.getAddress().toLowerCase().contains(constraintString)) {
                            newValues.add(favorite);
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof NavigationFavorite) {
                    return ((NavigationFavorite) resultValue).getAddress();
                } else {
                    return "";
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mFavorites = (ArrayList<NavigationFavorite>) results.values;
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

        ImageView icon = (ImageView) v.findViewById(R.id.icon);
        TextView name = (TextView) v.findViewById(R.id.name);
        TextView address = (TextView) v.findViewById(R.id.address);

        NavigationFavorite item = getItem(position);

        if (icon != null) {
            if (!item.getIcon().equals(Uri.EMPTY)) {
                icon.setVisibility(View.VISIBLE);
                icon.setImageURI(item.getIcon());
                if (item.getIcon().toString().startsWith("android.resource://nl.svendubbeld.car/")) {
                    icon.setColorFilter(getContext().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary}).getColor(0, Color.BLACK));
                } else {
                    icon.setColorFilter(null);
                }
            } else {
                icon.setVisibility(View.GONE);
                icon.setColorFilter(getContext().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary}).getColor(0, Color.BLACK));
            }
        }

        name.setText(item.getName());
        address.setText(item.getAddress());

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
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mFavorites.add(object);
            }
        }
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
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(position);
            } else {
                mFavorites.remove(position);
            }
        }
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
        mFavorites.clear();

        if (mIncludeContacts) {
            String home = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Preferences.PREF_KEY_NAVIGATION_HOME, "");
            String work = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Preferences.PREF_KEY_NAVIGATION_WORK, "");

            if (home != null && !home.isEmpty()) {
                mFavorites.add(new NavigationFavorite(mContext.getString(R.string.pref_title_navigation_home), home, Uri.parse("android.resource://nl.svendubbeld.car/" + R.drawable.ic_action_home)));
            }

            if (work != null && !work.isEmpty()) {
                mFavorites.add(new NavigationFavorite(mContext.getString(R.string.pref_title_navigation_work), work, Uri.parse("android.resource://nl.svendubbeld.car/" + R.drawable.ic_action_work)));
            }
        }

        mFavorites.addAll(mDb.getNavigationFavorites());

        if (mIncludeContacts) {
            mFavorites.addAll(getContacts());
        }
        notifyDataSetChanged();
    }

    private ArrayList<NavigationFavorite> getContacts() {
        ArrayList<NavigationFavorite> contacts = new ArrayList<>();

        Uri uri = StructuredPostal.CONTENT_URI;
        String[] projection = new String[]{
                StructuredPostal._ID,
                StructuredPostal.LOOKUP_KEY,
                StructuredPostal.DISPLAY_NAME,
                StructuredPostal.FORMATTED_ADDRESS,
                StructuredPostal.PHOTO_THUMBNAIL_URI
        };
        String sortOrder = StructuredPostal.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC";
        Cursor c = getContext().getContentResolver().query(uri, projection, null, null, sortOrder);
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndex(StructuredPostal.DISPLAY_NAME_PRIMARY));
            String address = c.getString(c.getColumnIndex(StructuredPostal.FORMATTED_ADDRESS));
            String icon = c.getString(c.getColumnIndex(StructuredPostal.PHOTO_THUMBNAIL_URI));

            if (icon == null) {
                icon = "android.resource://nl.svendubbeld.car/" + R.drawable.ic_social_person;
            }

            contacts.add(new NavigationFavorite(name, address, Uri.parse(icon)));

        }
        c.close();

        return contacts;
    }


    @Override
    public Filter getFilter() {
        return mFilter;
    }

    /**
     * An object containing data about a favorite for navigation.
     */
    public static class NavigationFavorite {

        private String mName;
        private String mAddress;
        private Uri mIcon;

        /**
         * Create a new favorite.
         *
         * @param name    The name of the favorite.
         * @param address The address of the favorite.
         */
        public NavigationFavorite(String name, String address) {
            init(name, address, Uri.EMPTY);
        }

        public NavigationFavorite(String name, String address, Uri icon) {
            init(name, address, icon);
        }

        private void init(String name, String address, Uri icon) {
            setName(name);
            setAddress(address);
            setIcon(icon);
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

        /**
         * @return The Ur
         */
        public Uri getIcon() {
            return mIcon;
        }

        public void setIcon(Uri icon) {
            mIcon = icon;
        }
    }

}
