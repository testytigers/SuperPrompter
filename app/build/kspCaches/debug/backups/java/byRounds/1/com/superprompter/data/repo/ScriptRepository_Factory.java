package com.superprompter.data.repo;

import com.superprompter.data.db.ScriptDao;
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
public final class ScriptRepository_Factory implements Factory<ScriptRepository> {
  private final Provider<ScriptDao> daoProvider;

  public ScriptRepository_Factory(Provider<ScriptDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ScriptRepository get() {
    return newInstance(daoProvider.get());
  }

  public static ScriptRepository_Factory create(javax.inject.Provider<ScriptDao> daoProvider) {
    return new ScriptRepository_Factory(Providers.asDaggerProvider(daoProvider));
  }

  public static ScriptRepository_Factory create(Provider<ScriptDao> daoProvider) {
    return new ScriptRepository_Factory(daoProvider);
  }

  public static ScriptRepository newInstance(ScriptDao dao) {
    return new ScriptRepository(dao);
  }
}
