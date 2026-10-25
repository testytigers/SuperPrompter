package com.superprompter.ui.scripts;

import com.superprompter.data.repo.ScriptRepository;
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
public final class ScriptListViewModel_Factory implements Factory<ScriptListViewModel> {
  private final Provider<ScriptRepository> repositoryProvider;

  public ScriptListViewModel_Factory(Provider<ScriptRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ScriptListViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static ScriptListViewModel_Factory create(
      javax.inject.Provider<ScriptRepository> repositoryProvider) {
    return new ScriptListViewModel_Factory(Providers.asDaggerProvider(repositoryProvider));
  }

  public static ScriptListViewModel_Factory create(Provider<ScriptRepository> repositoryProvider) {
    return new ScriptListViewModel_Factory(repositoryProvider);
  }

  public static ScriptListViewModel newInstance(ScriptRepository repository) {
    return new ScriptListViewModel(repository);
  }
}
