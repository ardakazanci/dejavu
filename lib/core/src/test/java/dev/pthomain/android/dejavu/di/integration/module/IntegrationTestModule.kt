/*
 *
 *  Copyright (C) 2017-2020 Pierre Thomain
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package dev.pthomain.android.dejavu.di.integration.module

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dev.pthomain.android.DejaVu
import dev.pthomain.android.boilerplate.core.utils.log.Logger
import dev.pthomain.android.dejavu.test.AssetHelper
import dev.pthomain.android.dejavu.test.network.MockClient
import dev.pthomain.android.dejavu.test.network.retrofit.TestClient
import dev.pthomain.android.glitchy.interceptor.error.glitch.Glitch
import okhttp3.OkHttpClient
import org.mockito.Mockito.mock
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
internal class IntegrationTestModule(private val dejaVu: dev.pthomain.android.DejaVu<Glitch>) {

    @Provides
    @Singleton
    fun provideDejaVu(): dev.pthomain.android.DejaVu<Glitch> = dejaVu

    @Provides
    @Singleton
    fun provideErrorFactory() =
            dejaVu.configuration.errorFactory

    @Provides
    @Singleton
    fun provideRetrofitCacheAdapterFactory(dejaVu: dev.pthomain.android.DejaVu<Glitch>) =
            dejaVu.retrofitCallAdapterFactory

    @Provides
    @Singleton
    fun provideMockClient() =
            MockClient()

    @Provides
    @Singleton
    fun provideOkHttp(mockClient: MockClient) =
            OkHttpClient.Builder().apply {
                addInterceptor(mockClient)
            }.build()

    @Provides
    @Singleton
    fun provideGson() =
            Gson()

    @Provides
    @Singleton
    fun provideGsonConverterFactory(gson: Gson) =
            GsonConverterFactory.create(gson)

    @Provides
    @Singleton
    fun provideRetrofit(adapterFactory: CallAdapter.Factory,
                        okHttpClient: OkHttpClient,
                        gsonConverterFactory: GsonConverterFactory) =
            Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(gsonConverterFactory)
                    .addCallAdapterFactory(adapterFactory)
                    .build()

    @Provides
    @Singleton
    fun provideTestClient(retrofit: Retrofit) =
            retrofit.create(TestClient::class.java)

    @Provides
    @Singleton
    fun provideLogger() =
            mock(Logger::class.java)

    @Provides
    @Singleton
    fun provideAssetHelper(gson: Gson) =
            AssetHelper(
                    ASSETS_FOLDER,
                    gson
            )
}

const val ASSETS_FOLDER = "src/main/assets/"
const val BASE_URL = "http://test.com"