package com.superprompter.overlay;

import com.superprompter.data.prefs.UserPreferencesRepository;
import com.superprompter.data.repo.ScriptRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class FloatingPrompterService_MembersInjector implements MembersInjector<FloatingPrompterService> {
  private final Provider<ScriptRepository> scriptRepositoryProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  public FloatingPrompterService_MembersInjector(
      Provider<ScriptRepository> scriptRepositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.scriptRepositoryProvider = scriptRepositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  public static MembersInjector<FloatingPrompterService> create(
      Provider<ScriptRepository> scriptRepositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new FloatingPrompterService_MembersInjector(scriptRepositoryProvider, preferencesRepositoryProvider);
  }

  public static MembersInjector<FloatingPrompterService> create(
      javax.inject.Provider<ScriptRepository> scriptRepositoryProvider,
      javax.inject.Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new FloatingPrompterService_MembersInjector(Providers.asDaggerProvider(scriptRepositoryProvider), Providers.asDaggerProvider(preferencesRepositoryProvider));
  }

  @Override
  public void injectMembers(FloatingPrompterService instance) {
    injectScriptRepository(instance, scriptRepositoryProvider.get());
    injectPreferencesRepository(instance, preferencesRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.superprompter.overlay.FloatingPrompterService.scriptRepository")
  public static void injectScriptRepository(FloatingPrompterService instance,
      ScriptRepository scriptRepository) {
    instance.scriptRepository = scriptRepository;
  }

  @InjectedFieldSignature("com.superprompter.overlay.FloatingPrompterService.preferencesRepository")
  public static void injectPreferencesRepository(FloatingPrompterService instance,
      UserPreferencesRepository preferencesRepository) {
    instance.preferencesRepository = preferencesRepository;
  }
}
