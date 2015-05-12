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
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import nl.svendubbeld.car.R;

import static android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
import static android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
import static android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY;
import static android.provider.ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY;
import static android.provider.ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER;
import static android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER;
import static android.provider.ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI;
import static android.provider.ContactsContract.CommonDataKinds.Phone.TYPE;
import static android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
import static android.provider.ContactsContract.CommonDataKinds.Phone._ID;

public class ContactsAdapter extends ArrayAdapter<ContactsAdapter.Contact> {

    private final Object mLock = new Object();

    private Filter mFilter;

    private ArrayList<Contact> mContacts = new ArrayList<>();
    private ArrayList<Contact> mOriginalValues;

    private int mResource;

    public ContactsAdapter(Context context, int resource) {
        super(context, resource);

        init(context, resource);
    }

    private void init(Context context, int resource) {
        mResource = resource;

        getContacts();

        mFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (mOriginalValues == null) {
                    synchronized (mLock) {
                        mOriginalValues = new ArrayList<>(mContacts);
                    }
                }

                if (constraint == null || constraint.length() == 0) {
                    ArrayList<Contact> list;
                    synchronized (mLock) {
                        list = new ArrayList<>(mOriginalValues);
                    }
                    results.values = list;
                    results.count = list.size();
                } else {
                    String constraintString = constraint.toString().toLowerCase();

                    ArrayList<Contact> values;
                    synchronized (mLock) {
                        values = new ArrayList<>(mOriginalValues);
                    }

                    ArrayList<Contact> newValues = new ArrayList<>();

                    for (Contact contact : values) {
                        if (contact.getName().toLowerCase().contains(constraintString) || contact.getPhone().toLowerCase().contains(constraintString) || contact.getNormalizedPhone().toLowerCase().contains(constraintString)) {
                            newValues.add(contact);
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof Contact) {
                    return ((Contact) resultValue).getPhone();
                } else {
                    return "";
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mContacts = (ArrayList<Contact>) results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView != null ? convertView : LayoutInflater.from(getContext()).inflate(mResource, parent, false);

        ImageView icon = (ImageView) v.findViewById(R.id.icon);
        TextView name = (TextView) v.findViewById(R.id.name);
        TextView phone = (TextView) v.findViewById(R.id.phone);

        Contact item = getItem(position);

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

        String phoneString = item.getPhone();
        CharSequence phoneTypeString = ContactsContract.CommonDataKinds.Phone.getTypeLabel(getContext().getResources(), item.getPhoneType(), "");
        if (phoneTypeString.length() > 0) {
            phoneString = phoneTypeString + " (" + phoneString + ")";
        }

        phone.setText(phoneString);

        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Contact getItem(int position) {
        return mContacts.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mContacts.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Contact object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mContacts.add(object);
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
                mContacts.remove(position);
            }
        }
    }

    private void getContacts() {
        clear();

        Uri uri = CONTENT_URI;
        String[] projection = new String[]{
                _ID,
                LOOKUP_KEY,
                DISPLAY_NAME,
                NUMBER,
                NORMALIZED_NUMBER,
                TYPE,
                PHOTO_THUMBNAIL_URI
        };
        String sortOrder = DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC";
        Cursor c = getContext().getContentResolver().query(uri, projection, null, null, sortOrder);
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndex(DISPLAY_NAME_PRIMARY));
            String phone = c.getString(c.getColumnIndex(NUMBER));
            String normalizedPhone = c.getString(c.getColumnIndex(NORMALIZED_NUMBER));
            int type = c.getInt(c.getColumnIndex(TYPE));
            String icon = c.getString(c.getColumnIndex(PHOTO_THUMBNAIL_URI));

            if (icon == null) {
                icon = "android.resource://nl.svendubbeld.car/" + R.drawable.ic_social_person;
            }

            add(new Contact(name, phone, normalizedPhone, type, Uri.parse(icon)));

        }
        c.close();

        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public static class Contact {

        private String mName;
        private String mPhone;
        private String mNormalizedPhone;
        private int mPhoneType;
        private Uri mIcon;

        public Contact(String name, String phone, String normalizedPhone) {
            init(name, phone, normalizedPhone, TYPE_OTHER, Uri.EMPTY);
        }

        public Contact(String name, String phone, String normalizedPhone, int phoneType) {
            init(name, phone, normalizedPhone, phoneType, Uri.EMPTY);
        }

        public Contact(String name, String phone, String normalizedPhone, Uri icon) {
            init(name, phone, normalizedPhone, TYPE_OTHER, icon);
        }

        public Contact(String name, String phone, String normalizedPhone, int phoneType, Uri icon) {
            init(name, phone, normalizedPhone, phoneType, icon);
        }

        private void init(String name, String phone, String normalizedPhone, int phoneType, Uri icon) {
            setName(name);
            setPhone(phone);
            setNormalizedPhone(normalizedPhone);
            setPhoneType(phoneType);
            setIcon(icon);
        }

        public Uri getIcon() {
            return mIcon;
        }

        public void setIcon(Uri icon) {
            mIcon = icon;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public String getPhone() {
            return mPhone;
        }

        public void setPhone(String phone) {
            mPhone = phone;
        }

        public String getNormalizedPhone() {
            return mNormalizedPhone;
        }

        public void setNormalizedPhone(String normalizedPhone) {
            mNormalizedPhone = normalizedPhone;
        }

        public int getPhoneType() {
            return mPhoneType;
        }

        public void setPhoneType(int phoneType) {
            mPhoneType = phoneType;
        }

    }

}
