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

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.fragment.NavigationFavoritesFragment;
import nl.svendubbeld.car.fragment.NavigationInputFragment;

/**
 * PagerAdapter for starting navigation.
 */
public class PlacesFragmentPagerAdapter extends FragmentPagerAdapter {

    private Fragment mFavorites;
    private Fragment mInput;

    private Context mContext;

    public PlacesFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        mContext = context;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return getFavorites();
            case 1:
                return getInput();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.navigation_favorites);
            case 1:
                return mContext.getString(R.string.navigation_input);
        }

        return super.getPageTitle(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return 2;
    }

    /**
     * @return {@link NavigationFavoritesFragment}
     */
    private Fragment getFavorites() {
        if (mFavorites == null) {
            mFavorites = new NavigationFavoritesFragment();
        }
        return mFavorites;
    }

    /**
     * @return {@link NavigationInputFragment}
     */
    private Fragment getInput() {
        if (mInput == null) {
            mInput = new NavigationInputFragment();
        }
        return mInput;
    }
}