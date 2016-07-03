package nl.svendubbeld.car;

import nl.svendubbeld.car.database.NavigationFavoritesRepository;
import nl.svendubbeld.car.database.SqlNavigationFavoritesRepository;
import nl.svendubbeld.inject.Injector;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Injector.INSTANCE.register(NavigationFavoritesRepository.class, SqlNavigationFavoritesRepository.class);
    }
}
