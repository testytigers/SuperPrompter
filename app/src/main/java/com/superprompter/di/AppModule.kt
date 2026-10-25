package com.superprompter.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.superprompter.data.db.ScriptDao
import com.superprompter.data.db.SuperPrompterDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SuperPrompterDatabase =
        Room.databaseBuilder(
            context,
            SuperPrompterDatabase::class.java,
            SuperPrompterDatabase.DATABASE_NAME,
        ).build()

    @Provides
    fun provideScriptDao(database: SuperPrompterDatabase): ScriptDao = database.scriptDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_preferences") },
        )
}
