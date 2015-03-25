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

package nl.svendubbeld.car.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.adapter.NavigationFavoritesAdapter;

public class NavigationFavoritesPreferenceActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ListView mListView;
    NavigationFavoritesAdapter mAdapter;
    EditText mName;
    EditText mAddress;
    Button mAdd;

    int mEdit = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_navigation_favorites_preference);

        mAdapter = new NavigationFavoritesAdapter(this, R.layout.list_item_navigation_favorite);

        mListView = (ListView) findViewById(android.R.id.list);
        mName = (EditText) findViewById(R.id.name);
        mAddress = (EditText) findViewById(R.id.address);
        mAdd = (Button) findViewById(R.id.btn_add);

        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(findViewById(android.R.id.empty));
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        mAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    add();
                    return true;
                }

                return false;
            }
        });
    }

    void save() {
        mAdapter.saveToDatabase();
    }

    void add() {
        String name = mName.getText().toString();
        String address = mAddress.getText().toString();

        if (!name.isEmpty() && !address.isEmpty()) {
            mAdapter.add(new NavigationFavoritesAdapter.NavigationFavorite(name, address));
            mAdapter.notifyDataSetChanged();

            mName.setText("");
            mAddress.setText("");

            mName.requestFocus();
        }
    }

    void edit() {
        String name = mName.getText().toString();
        String address = mAddress.getText().toString();

        if (!name.isEmpty() && !address.isEmpty()) {
            mAdapter.edit(mEdit, name, address);
            mAdapter.notifyDataSetChanged();

            mEdit = -1;
            mAdd.setText(R.string.add);

            mName.setText("");
            mAddress.setText("");

            mName.requestFocus();
        }
    }

    void startEditMode(int position) {
        mEdit = position;

        mName.setText(mAdapter.getItem(position).getName());
        mAddress.setText(mAdapter.getItem(position).getAddress());

        mAdd.setText(R.string.edit);
    }

    void delete(int position) {
        mAdapter.remove(position);
        mAdapter.notifyDataSetChanged();

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                if (mEdit > -1) {
                    edit();
                } else {
                    add();
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        save();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.loadFromDatabase();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startEditMode(position);
    }

    @Override
    public void onBackPressed() {
        if (mEdit > -1) {
            mEdit = -1;
            mAdd.setText(R.string.add);

            mName.setText("");
            mAddress.setText("");

            mName.requestFocus();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(this)
                .setItems(R.array.pref_navigation_long_click_items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Edit
                                startEditMode(position);
                                break;
                            case 1:
                                // Delete
                                delete(position);
                                break;
                            case 2:
                                // Cancel
                                dialog.cancel();
                        }
                    }
                })
                .setTitle(mAdapter.getItem(position).getName())
                .setPositiveButton(null, null)
                .setNegativeButton(null, null)
                .show();
        return true;
    }
}
