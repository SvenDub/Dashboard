package nl.svendubbeld.car.database;

import android.content.Context;

import java.util.Collections;
import java.util.List;

import nl.svendubbeld.car.adapter.NavigationFavoritesAdapter;

public class SqlNavigationFavoritesRepository implements NavigationFavoritesRepository {

    private Context mContext;

    public SqlNavigationFavoritesRepository() {
    }

    @Override
    public void set(List<NavigationFavoritesAdapter.NavigationFavorite> favorites) {

    }

    @Override
    public List<NavigationFavoritesAdapter.NavigationFavorite> getAll() {
        return Collections.emptyList();
    }

    @Override
    public void setContext(Context context) {
        mContext = context;
    }
}
