package nl.svendubbeld.car.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

        mAdapter = new NavigationFavoritesAdapter(getActivity(), R.layout.list_item_navigation_favorite, false);

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
}