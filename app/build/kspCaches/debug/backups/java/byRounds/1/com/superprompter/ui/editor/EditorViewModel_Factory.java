package com.superprompter.ui.editor;

import android.content.Context;
import androidx.lifecycle.SavedStateHandle;
import com.superprompter.data.repo.ScriptRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class EditorViewModel_Factory implements Factory<EditorViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ScriptRepository> repositoryProvider;

  private final Provider<Context> contextProvider;

  public EditorViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ScriptRepository> repositoryProvider, Provider<Context> contextProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.repositoryProvider = repositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public EditorViewModel get() {
    return newInstance(savedStateHandleProvider.get(), repositoryProvider.get(), contextProvider.get());
  }

  public static EditorViewModel_Factory create(
      javax.inject.Provider<SavedStateHandle> savedStateHandleProvider,
      javax.inject.Provider<ScriptRepository> repositoryProvider,
      javax.inject.Provider<Context> contextProvider) {
    return new EditorViewModel_Factory(Providers.asDaggerProvider(savedStateHandleProvider), Providers.asDaggerProvider(repositoryProvider), Providers.asDaggerProvider(contextProvider));
  }

  public static EditorViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ScriptRepository> repositoryProvider, Provider<Context> contextProvider) {
    return new EditorViewModel_Factory(savedStateHandleProvider, repositoryProvider, contextProvider);
  }

  public static EditorViewModel newInstance(SavedStateHandle savedStateHandle,
      ScriptRepository repository, Context context) {
    return new EditorViewModel(savedStateHandle, repository, context);
  }
}
