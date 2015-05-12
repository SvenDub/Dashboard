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
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.adapter.ContactsAdapter;

/**
 * Activity that shows a dialer.
 */
public class DialerActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final int SPEECH_REQUEST_CODE = 0;

    // Input
    private AutoCompleteTextView mInput;
    private ImageView mInputVoice;
    private ImageView mInputDelete;

    private FrameLayout mMostRecentContainer;
    private ImageView mMostRecentIcon;
    private TextView mMostRecentName;
    private TextView mMostRecentPhone;
    private ImageView mMostRecentType;
    private ContactsAdapter.Contact mMostRecent;

    // Dialpad
    private GridLayout mDialpad;
    private TextView mDialpad0;
    private TextView mDialpad1;
    private TextView mDialpad2;
    private TextView mDialpad3;
    private TextView mDialpad4;
    private TextView mDialpad5;
    private TextView mDialpad6;
    private TextView mDialpad7;
    private TextView mDialpad8;
    private TextView mDialpad9;
    private TextView mDialpadStar;
    private TextView mDialpadPound;

    /**
     * Sets the layout.
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
        setContentView(R.layout.activity_dialer);

        // Get input views
        mInput = (AutoCompleteTextView) findViewById(R.id.input);
        mInputDelete = (ImageView) findViewById(R.id.input_delete);
        mInputVoice = (ImageView) findViewById(R.id.input_voice);

        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasInput = s.length() > 0;
                mInputDelete.setVisibility(hasInput ? View.VISIBLE : View.GONE);
                mInputVoice.setVisibility(hasInput ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mInput.setAdapter(new ContactsAdapter(this, R.layout.list_item_contact));
        mInput.setOnItemClickListener(this);

        mInputDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mInput.setText("");
                return true;
            }
        });

        mMostRecentContainer = (FrameLayout) findViewById(R.id.contact_most_recent);
        mMostRecentIcon = (ImageView) mMostRecentContainer.findViewById(R.id.icon);
        mMostRecentName = (TextView) mMostRecentContainer.findViewById(R.id.name);
        mMostRecentPhone = (TextView) mMostRecentContainer.findViewById(R.id.phone);
        mMostRecentType = (ImageView) mMostRecentContainer.findViewById(R.id.type);

        mMostRecentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call(mMostRecent.getPhone());
            }
        });

        // Get dialpad views
        mDialpad = (GridLayout) findViewById(R.id.dialpad);
        mDialpad0 = (TextView) findViewById(R.id.dialpad_0);
        mDialpad1 = (TextView) findViewById(R.id.dialpad_1);
        mDialpad2 = (TextView) findViewById(R.id.dialpad_2);
        mDialpad3 = (TextView) findViewById(R.id.dialpad_3);
        mDialpad4 = (TextView) findViewById(R.id.dialpad_4);
        mDialpad5 = (TextView) findViewById(R.id.dialpad_5);
        mDialpad6 = (TextView) findViewById(R.id.dialpad_6);
        mDialpad7 = (TextView) findViewById(R.id.dialpad_7);
        mDialpad8 = (TextView) findViewById(R.id.dialpad_8);
        mDialpad9 = (TextView) findViewById(R.id.dialpad_9);
        mDialpadStar = (TextView) findViewById(R.id.dialpad_star);
        mDialpadPound = (TextView) findViewById(R.id.dialpad_pound);

        mDialpad0.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mInput.append("+");
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri uri = CallLog.Calls.CONTENT_URI;
        String[] projection = new String[]{
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NORMALIZED_NUMBER,
                CallLog.Calls.CACHED_NUMBER_TYPE,
                CallLog.Calls.CACHED_PHOTO_ID,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE
        };
        Cursor c = getContentResolver().query(uri, projection, null, null, null);
        if (c.moveToLast()) {
            String name = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));
            String phone = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
            String normalizedPhone = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NORMALIZED_NUMBER));
            int phoneType = c.getInt(c.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE));
            long photoId = c.getLong(c.getColumnIndex(CallLog.Calls.CACHED_PHOTO_ID));
            int type = c.getInt(c.getColumnIndex(CallLog.Calls.TYPE));
            long date = c.getLong(c.getColumnIndex(CallLog.Calls.DATE));

            Uri icon;

            if (photoId != 0) {
                icon = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId);
                mMostRecentIcon.setColorFilter(null);
            } else {
                icon = Uri.parse("android.resource://nl.svendubbeld.car/" + R.drawable.ic_social_person);
                mMostRecentIcon.setColorFilter(obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary}).getColor(0, Color.BLACK));
            }

            mMostRecent = new ContactsAdapter.Contact(name, phone, normalizedPhone, phoneType, icon);

            mMostRecentIcon.setImageURI(icon);

            if (name == null) {
                name = phone;
            }

            mMostRecentName.setText(name);

            CharSequence phoneTypeString = ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), phoneType, "");
            if (phoneTypeString.length() > 0) {
                phone = String.valueOf(phoneTypeString);
            }

            phone += ", " + DateUtils.getRelativeTimeSpanString(date);

            mMostRecentPhone.setText(phone);

            switch (type) {
                default:
                case CallLog.Calls.INCOMING_TYPE:
                    mMostRecentType.setImageResource(R.drawable.ic_communication_call_received);
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    mMostRecentType.setImageResource(R.drawable.ic_communication_call_made);
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    mMostRecentType.setImageResource(R.drawable.ic_communication_call_missed);
                    break;
            }

            mMostRecentType.setVisibility(View.VISIBLE);

            mMostRecentContainer.setVisibility(View.VISIBLE);
        } else {
            mMostRecentContainer.setVisibility(View.GONE);
        }
        c.close();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            View anchor = findViewById(mInput.getDropDownAnchor());

            int height = mDialpad.getTop() - (anchor.getBottom() + mInput.getDropDownVerticalOffset());
            mInput.setDropDownHeight(height);
        }
    }

    /**
     * Initiates a call to the specified number.
     *
     * @param number The phone number as a string, without the "tel:" URI scheme. E.g.: "+31 6
     *               12345678"
     */
    private void call(CharSequence number) {
        call(Uri.parse("tel:" + number));
    }

    /**
     * Initiates a call to the specified number.
     *
     * @param number The phone number as a string, without the "tel:" URI scheme. E.g.: {@code "+31
     *               6 12345678"}
     */
    private void call(String number) {
        call(Uri.parse("tel:" + number));
    }

    /**
     * Initiates a call to the specified number.
     *
     * @param number The phone number as URI. E.g.: {@code "tel:+31 6 12345678"}
     */
    private void call(Uri number) {
        Intent dial = new Intent(Intent.ACTION_CALL, number);
        startActivity(dial);
    }

    /**
     * Called when a dialpad button has been touched.
     *
     * @param v The button that has ben touched.
     */
    public void dialpadButton(View v) {
        mInput.append(((TextView) v).getText().toString());
    }

    /**
     * Called when an input button has been touched.
     *
     * @param v The button that has ben touched.
     */
    public void inputButton(View v) {
        CharSequence number = mInput.getText();

        switch (v.getId()) {
            case R.id.input_delete:
                if (number.length() > 0) {
                    // Remove one character from the end
                    mInput.setText(number.subSequence(0, number.length() - 1));
                    mInput.setSelection(number.length() - 1);
                }
                break;
            case R.id.input_voice:
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                startActivityForResult(intent, SPEECH_REQUEST_CODE);
                break;
            case R.id.btn_dialer:
                call(number);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ContactsAdapter.Contact contact = (ContactsAdapter.Contact) mInput.getAdapter().getItem(position);
        call(contact.getPhone());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            mInput.setText(spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
