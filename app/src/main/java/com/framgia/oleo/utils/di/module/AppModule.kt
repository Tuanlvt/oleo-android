package com.framgia.oleo.utils.di.module

import android.app.Application
import android.content.Context
import com.framgia.oleo.data.source.remote.api.middleware.BooleanAdapter
import com.framgia.oleo.data.source.remote.api.middleware.DoubleAdapter
import com.framgia.oleo.data.source.remote.api.middleware.IntegerAdapter
import com.framgia.oleo.utils.rxAndroid.BaseScheduleProvider
import com.framgia.oleo.utils.rxAndroid.SchedulerProvider
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context =
        application.applicationContext

    @Provides
    @Singleton
    fun provideBaseSchedulerProvider(): BaseScheduleProvider =
        SchedulerProvider.instance

    @Singleton
    @Provides
    fun provideGson(): Gson {
        val booleanAdapter = BooleanAdapter()
        val integerAdapter = IntegerAdapter()
        val doubleAdapter = DoubleAdapter()
        return GsonBuilder()
            .registerTypeAdapter(Boolean::class.java, booleanAdapter)
            .registerTypeAdapter(Int::class.java, integerAdapter)
            .registerTypeAdapter(Double::class.java, doubleAdapter)
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .excludeFieldsWithoutExposeAnnotation()
            .create()
    }
}