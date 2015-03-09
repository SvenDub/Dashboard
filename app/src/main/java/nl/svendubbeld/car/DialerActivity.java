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

package nl.svendubbeld.car;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DialerActivity extends Activity {


    ListView mSuggestions;

    TextView mInput;
    ImageView mInputDelete;

    TextView mDialpad0;
    TextView mDialpad1;
    TextView mDialpad2;
    TextView mDialpad3;
    TextView mDialpad4;
    TextView mDialpad5;
    TextView mDialpad6;
    TextView mDialpad7;
    TextView mDialpad8;
    TextView mDialpad9;
    TextView mDialpadStar;
    TextView mDialpadPound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_dialer);

        mSuggestions = (ListView) findViewById(R.id.suggestions);

        mInput = (TextView) findViewById(R.id.input);
        mInputDelete = (ImageView) findViewById(R.id.input_delete);

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


        mInputDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mInput.setText("");
                return true;
            }
        });
    }

    /**
     * Initiates a call to the specified number.
     *
     * @param number The phone number as a string, without the "tel:" URI scheme. E.g.: "+31 6 12345678"
     */
    private void call(CharSequence number) {
        call(Uri.parse("tel:" + number));
    }

    /**
     * Initiates a call to the specified number.
     *
     * @param number The phone number as a string, without the "tel:" URI scheme. E.g.: "+31 6 12345678"
     */
    private void call(String number) {
        call(Uri.parse("tel:" + number));
    }

    /**
     * Initiates a call to the specified number.
     *
     * @param number The phone number as URI. E.g.: "tel:+31 6 12345678"
     */
    private void call(Uri number) {
        Intent dial = new Intent(Intent.ACTION_CALL, number);
        startActivity(dial);
    }

    public void dialpadButton(View v) {
        mInput.append(((TextView) v).getText().toString());
    }

    public void inputButton(View v) {
        CharSequence number = mInput.getText();

        switch (v.getId()) {
            case R.id.input_delete:
                if (number.length() > 0) {
                    mInput.setText(number.subSequence(0, number.length() - 1));
                }
                break;
            case R.id.btn_dialer:
                call(number);
                break;
        }
    }
}
