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

package nl.svendubbeld.car.fragment;

import android.app.ListFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Space;

import nl.svendubbeld.car.Log;
import nl.svendubbeld.car.OnTargetChangeListener;
import nl.svendubbeld.car.R;
import nl.svendubbeld.car.adapter.NavigationFavoritesAdapter;

/**
 * Fragment containing {@link nl.svendubbeld.car.adapter.NavigationFavoritesAdapter.NavigationFavorite}.
 */
public class NavigationFavoritesFragment extends ListFragment {

    private NavigationFavoritesAdapter mAdapter;

    /**
     * Sets the adapter.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new NavigationFavoritesAdapter(getActivity(), R.layout.list_item_navigation_favorite);

        setListAdapter(mAdapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_favorites, container, false);
    }

    /**
     * This method will be called when an item in the list is selected. Starts navigation to the
     * selected favorite.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        NavigationFavoritesAdapter.NavigationFavorite favorite = mAdapter.getItem(position);

        if (getActivity() instanceof OnTargetChangeListener) {
            ((OnTargetChangeListener) getActivity()).onTargetChanged(favorite.getAddress());
        } else {
            Log.e(NavigationFavoritesFragment.class.getSimpleName(), "Parent activity should implement OnTargetChangeListener!");
        }
    }

    /**
     * Adds some padding to compensate for the nav bar.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater,
     *                           ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     *                           saved state as given here.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView =  ((ListView) view.findViewById(android.R.id.list));

        listView.setFooterDividersEnabled(false);

        // Add padding to compensate for the nav bar.
        if ((getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) || (getActivity().getResources().getConfiguration().smallestScreenWidthDp >= 600)) {
            Space space = new Space(getActivity());
            space.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.nav_bar_height));

           listView.addFooterView(space);
        }
    }
}
