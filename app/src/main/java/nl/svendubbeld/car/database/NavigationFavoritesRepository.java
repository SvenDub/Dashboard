package nl.svendubbeld.car.database;

import android.content.Context;

import java.util.List;

import nl.svendubbeld.car.adapter.NavigationFavoritesAdapter;

/**
 * Repository containing favorite navigation destinations.
 */
public interface NavigationFavoritesRepository {

    /**
     * Overwrites the list of favorite navigation destinations.
     *
     * @param favorites The new list of {@link nl.svendubbeld.car.adapter.NavigationFavoritesAdapter.NavigationFavorite NavigationFavorites}.
     */
    void set(List<NavigationFavoritesAdapter.NavigationFavorite> favorites);

    /**
     * Gets all favorite navigation destinations.
     *
     * @return All {@link nl.svendubbeld.car.adapter.NavigationFavoritesAdapter.NavigationFavorite NavigationFavorites}.
     */
    List<NavigationFavoritesAdapter.NavigationFavorite> getAll();

    /**
     * Sets the context to use when querying.
     * @param context The context to use for querying.
     */
    void setContext(Context context);
}
