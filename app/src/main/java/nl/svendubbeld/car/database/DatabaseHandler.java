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

package nl.svendubbeld.car.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;

import nl.svendubbeld.car.adapter.NavigationFavoritesAdapter;

/**
 * API for interacting with the database.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    /**
     * The name of the database.
     */
    public static final String DATABASE_NAME = "Dashboard.db";
    /**
     * The version of the database.
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Constant for text column type.
     */
    private static final String TYPE_TEXT = " TEXT";
    /**
     * Constant for integer column type.
     */
    private static final String TYPE_INT = " INTEGER";
    /**
     * Constant for comma separator.
     */
    private static final String COMMA_SEP = ",";

    /**
     * SQL query for creating {@link nl.svendubbeld.car.database.DatabaseHandler.Contract.NavigationFavoriteEntry}.
     */
    private static final String SQL_CREATE_NAVIGATION_FAVORITES =
            "CREATE TABLE " + Contract.NavigationFavoriteEntry.TABLE_NAME + " (" +
                    Contract.NavigationFavoriteEntry._ID + TYPE_INT + " PRIMARY KEY" + COMMA_SEP +
                    Contract.NavigationFavoriteEntry.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
                    Contract.NavigationFavoriteEntry.COLUMN_NAME_ADDRESS + TYPE_TEXT;

    /**
     * Create a new DatabaseHandler.
     *
     * @param context The current context.
     */
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NAVIGATION_FAVORITES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Gets all favorites currently saved in the database.
     *
     * @return An {@link ArrayList} containing all {@link nl.svendubbeld.car.adapter.NavigationFavoritesAdapter.NavigationFavorite}.
     */
    public ArrayList<NavigationFavoritesAdapter.NavigationFavorite> getNavigationFavorites() {
        SQLiteDatabase db = getReadableDatabase();

        // Columns to fetch
        String[] projection = {
                Contract.NavigationFavoriteEntry.COLUMN_NAME_NAME,
                Contract.NavigationFavoriteEntry.COLUMN_NAME_ADDRESS
        };

        // Execute query
        Cursor cursor = db.query(
                Contract.NavigationFavoriteEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        ArrayList<NavigationFavoritesAdapter.NavigationFavorite> navigationFavorites = new ArrayList<>(cursor.getCount());

        // Add all favorites to the list
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.NavigationFavoriteEntry.COLUMN_NAME_NAME));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(Contract.NavigationFavoriteEntry.COLUMN_NAME_ADDRESS));

            navigationFavorites.add(new NavigationFavoritesAdapter.NavigationFavorite(name, address));
            cursor.moveToNext();
        }

        db.close();
        return navigationFavorites;
    }

    /**
     * Replaces all old favorites with the supplied list.
     *
     * @param favorites The new list of {@link nl.svendubbeld.car.adapter.NavigationFavoritesAdapter.NavigationFavorite}.
     */
    public void setNavigationFavorites(ArrayList<NavigationFavoritesAdapter.NavigationFavorite> favorites) {
        SQLiteDatabase db = getWritableDatabase();

        // Remove old favorites
        db.delete(Contract.NavigationFavoriteEntry.TABLE_NAME, null, null);

        // Insert each new favorite
        for (NavigationFavoritesAdapter.NavigationFavorite favorite : favorites) {
            ContentValues values = new ContentValues();
            values.put(Contract.NavigationFavoriteEntry.COLUMN_NAME_NAME, favorite.getName());
            values.put(Contract.NavigationFavoriteEntry.COLUMN_NAME_ADDRESS, favorite.getAddress());

            db.insert(Contract.NavigationFavoriteEntry.TABLE_NAME, null, values);
        }

        db.close();
    }

    /**
     * Contract containing constants for all tables and columns.
     */
    public static final class Contract {

        public Contract() {

        }

        /**
         * The table containing {@link nl.svendubbeld.car.adapter.NavigationFavoritesAdapter.NavigationFavorite}.
         */
        public static abstract class NavigationFavoriteEntry implements BaseColumns {
            /**
             * Constant for the table name.
             */
            public static final String TABLE_NAME = "navigation_favorites";
            /**
             * Constant for the name column.
             */
            public static final String COLUMN_NAME_NAME = "name";
            /**
             * Constant for the address column.
             */
            public static final String COLUMN_NAME_ADDRESS = "address";
        }

    }
}
