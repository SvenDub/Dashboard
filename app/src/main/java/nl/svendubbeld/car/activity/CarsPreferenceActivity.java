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
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toolbar;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.adapter.CarAdapter;

public class CarsPreferenceActivity extends Activity implements AdapterView.OnItemLongClickListener {

    private Context mContext = this;

    private ListView mListView;
    private CarAdapter mAdapter;

    /**
     * Sets the layout and loads the list.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in {@link #onSaveInstanceState(Bundle)}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make all system bars transparent and draw behind them
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // Set layout
        setContentView(R.layout.activity_car_preference);

        // Set action bar
        setActionBar((Toolbar) findViewById(R.id.toolbar));

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get views
        mListView = (ListView) findViewById(android.R.id.list);

        // Load the list
        mAdapter = new CarAdapter(this, R.layout.list_item_car);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(findViewById(android.R.id.empty));
        mListView.setOnItemLongClickListener(this);

        mListView.setFooterDividersEnabled(false);

        // Add padding to compensate for the nav bar.
        if ((getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) || (getResources().getConfiguration().smallestScreenWidthDp >= 600)) {
            Space v = new Space(this);
            v.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.nav_bar_height));

            mListView.addFooterView(v);
        }
    }

    /**
     * Saves the list.
     */
    @Override
    protected void onPause() {
        super.onPause();

        save();
    }

    /**
     * Reload the list.
     */
    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.loadFromDatabase();
    }


    /**
     * Requests the adapter to save the current list to the database.
     */
    private void save() {
        mAdapter.saveToDatabase();
        new BackupManager(this).dataChanged();
    }

    /**
     * Modifies an item in the adapter.
     */
    private void edit(int position, String name) {
        if (!name.isEmpty()) {
            mAdapter.getItem(position).setName(name);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Removes an item from the adapter.
     *
     * @param position The position of the item to remove.
     */
    private void delete(int position) {
        mAdapter.remove(position);
        mAdapter.notifyDataSetChanged();

    }

    /**
     * <p>Callback method to be invoked when an item in this view has been clicked and held.</p>
     *
     * <p>Shows a popup menu with additional options.</p>
     *
     * @param parent   The AbsListView where the click happened
     * @param view     The view within the AbsListView that was clicked
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     * @return true if the callback consumed the long click, false otherwise
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(this)
                .setItems(R.array.pref_navigation_long_click_items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Edit
                                View root = getLayoutInflater().inflate(R.layout.dialog_edit_car, null);

                                final EditText name = (EditText) root.findViewById(R.id.name);

                                final AlertDialog editDialog = new AlertDialog.Builder(mContext)
                                        .setTitle(getString(R.string.edit) + " " + mAdapter.getItem(position).getVin())
                                        .setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                edit(position, name.getText().toString());
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, null)
                                        .setView(root)
                                        .create();

                                TextWatcher textWatcher = new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        editDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(name.length() > 0);
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                    }
                                };

                                name.addTextChangedListener(textWatcher);

                                name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                    @Override
                                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                                            Button button = editDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                            if (button.isEnabled()) {
                                                button.callOnClick();
                                            }

                                            return true;
                                        }

                                        return false;
                                    }
                                });

                                editDialog.show();
                                editDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(name.length() > 0);

                                name.setText(mAdapter.getItem(position).getName());

                                editDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
