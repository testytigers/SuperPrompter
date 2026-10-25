package com.superprompter.ui.prompter;

import androidx.lifecycle.SavedStateHandle;
import com.superprompter.data.prefs.UserPreferencesRepository;
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
public final class PrompterViewModel_Factory implements Factory<PrompterViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ScriptRepository> scriptRepositoryProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  public PrompterViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ScriptRepository> scriptRepositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.scriptRepositoryProvider = scriptRepositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  @Override
  public PrompterViewModel get() {
    return newInstance(savedStateHandleProvider.get(), scriptRepositoryProvider.get(), preferencesRepositoryProvider.get());
  }

  public static PrompterViewModel_Factory create(
      javax.inject.Provider<SavedStateHandle> savedStateHandleProvider,
      javax.inject.Provider<ScriptRepository> scriptRepositoryProvider,
      javax.inject.Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new PrompterViewModel_Factory(Providers.asDaggerProvider(savedStateHandleProvider), Providers.asDaggerProvider(scriptRepositoryProvider), Providers.asDaggerProvider(preferencesRepositoryProvider));
  }

  public static PrompterViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ScriptRepository> scriptRepositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new PrompterViewModel_Factory(savedStateHandleProvider, scriptRepositoryProvider, preferencesRepositoryProvider);
  }

  public static PrompterViewModel newInstance(SavedStateHandle savedStateHandle,
      ScriptRepository scriptRepository, UserPreferencesRepository preferencesRepository) {
    return new PrompterViewModel(savedStateHandle, scriptRepository, preferencesRepository);
  }
}
