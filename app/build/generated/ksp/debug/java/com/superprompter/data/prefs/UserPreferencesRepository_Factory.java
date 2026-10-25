package com.superprompter.data.prefs;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class UserPreferencesRepository_Factory implements Factory<UserPreferencesRepository> {
  private final Provider<DataStore<Preferences>> dataStoreProvider;

  public UserPreferencesRepository_Factory(Provider<DataStore<Preferences>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public UserPreferencesRepository get() {
    return newInstance(dataStoreProvider.get());
  }

  public static UserPreferencesRepository_Factory create(
      javax.inject.Provider<DataStore<Preferences>> dataStoreProvider) {
    return new UserPreferencesRepository_Factory(Providers.asDaggerProvider(dataStoreProvider));
  }

  public static UserPreferencesRepository_Factory create(
      Provider<DataStore<Preferences>> dataStoreProvider) {
    return new UserPreferencesRepository_Factory(dataStoreProvider);
  }

  public static UserPreferencesRepository newInstance(DataStore<Preferences> dataStore) {
    return new UserPreferencesRepository(dataStore);
  }
}
