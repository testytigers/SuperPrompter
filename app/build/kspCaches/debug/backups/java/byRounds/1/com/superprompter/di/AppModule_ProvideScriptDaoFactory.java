package com.superprompter.di;

import com.superprompter.data.db.ScriptDao;
import com.superprompter.data.db.SuperPrompterDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideScriptDaoFactory implements Factory<ScriptDao> {
  private final Provider<SuperPrompterDatabase> databaseProvider;

  public AppModule_ProvideScriptDaoFactory(Provider<SuperPrompterDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ScriptDao get() {
    return provideScriptDao(databaseProvider.get());
  }

  public static AppModule_ProvideScriptDaoFactory create(
      javax.inject.Provider<SuperPrompterDatabase> databaseProvider) {
    return new AppModule_ProvideScriptDaoFactory(Providers.asDaggerProvider(databaseProvider));
  }

  public static AppModule_ProvideScriptDaoFactory create(
      Provider<SuperPrompterDatabase> databaseProvider) {
    return new AppModule_ProvideScriptDaoFactory(databaseProvider);
  }

  public static ScriptDao provideScriptDao(SuperPrompterDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideScriptDao(database));
  }
}
