package com.superprompter.ui.settings;

import com.superprompter.data.prefs.UserPreferencesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserPreferencesRepository> repositoryProvider;

  public SettingsViewModel_Factory(Provider<UserPreferencesRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(
      javax.inject.Provider<UserPreferencesRepository> repositoryProvider) {
    return new SettingsViewModel_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static SettingsViewModel_Factory create(
      Provider<UserPreferencesRepository> repositoryProvider) {
    return new SettingsViewModel_Factory(repositoryProvider);
  }

  public static SettingsViewModel newInstance(UserPreferencesRepository repository) {
    return new SettingsViewModel(repository);
  }
}
