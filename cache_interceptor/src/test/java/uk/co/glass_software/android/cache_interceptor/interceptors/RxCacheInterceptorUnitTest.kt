/*
 * Copyright (C) 2017 Glass Software Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package uk.co.glass_software.android.cache_interceptor.interceptors

import io.reactivex.Observable
import io.reactivex.ObservableSource
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import uk.co.glass_software.android.boilerplate.utils.log.Logger
import uk.co.glass_software.android.cache_interceptor.base.network.model.TestResponse
import uk.co.glass_software.android.cache_interceptor.configuration.CacheInstruction
import uk.co.glass_software.android.cache_interceptor.interceptors.internal.cache.CacheInterceptor
import uk.co.glass_software.android.cache_interceptor.interceptors.internal.cache.CacheTokenHelper
import uk.co.glass_software.android.cache_interceptor.interceptors.internal.cache.token.CacheToken
import uk.co.glass_software.android.cache_interceptor.interceptors.internal.error.ApiError
import uk.co.glass_software.android.cache_interceptor.interceptors.internal.error.ErrorInterceptor

class RxCacheInterceptorUnitTest {

    private var responseClass: Class<TestResponse>? = null
    private val mockUrl = "mockUrl"
    private val mockBody = "mockBody"
    private var mockLogger: Logger? = null
    private var mockErrorInterceptorFactory: ErrorInterceptor.Factory<ApiError>? = null
    private var mockCacheInterceptorFactory: CacheInterceptor.Factory<ApiError>? = null
    private var mockErrorInterceptor: ErrorInterceptor<TestResponse, ApiError>? = null
    private var mockCacheInterceptor: CacheInterceptor<ApiError, TestResponse>? = null
    private var mockObservable: Observable<TestResponse>? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        responseClass = TestResponse::class.java
        mockLogger = mock(Logger::class.java)
        mockErrorInterceptorFactory = mock(ErrorInterceptor.Factory::class.java)
        mockCacheInterceptorFactory = mock(CacheInterceptor.Factory::class.java)
        mockErrorInterceptor = mock(ErrorInterceptor<*>::class.java)
        mockCacheInterceptor = mock(CacheInterceptor<*>::class.java)
        mockObservable = Observable.just(mock(TestResponse::class.java))

        `when`<Observable<ResponseWrapper<TestResponse>>>(mockErrorInterceptor!!.apply(any<Observable<Any>>())).thenReturn(mockObservable)
        `when`<ObservableSource<ResponseWrapper<ApiError>>>(mockCacheInterceptor!!.apply(any<Observable<ResponseWrapper<ApiError>>>())).thenReturn(mockObservable)

        `when`<Any>(mockErrorInterceptorFactory!!.create(any<T>())).thenReturn(mockErrorInterceptor)
        `when`<Any>(mockCacheInterceptorFactory!!.create(any<T>(), any<T>())).thenReturn(mockCacheInterceptor)
    }

    private fun getTarget(isRefresh: Boolean): RxCacheInterceptor<*> {
        return RxCacheInterceptor(
                true,
                responseClass,
                mockUrl,
                mockBody,
                isRefresh,
                mockLogger,
                mockErrorInterceptorFactory,
                mockCacheInterceptorFactory
        )
    }

    @Test
    @Throws(Exception::class)
    fun testApplyIsRefreshTrue() {
        testApply(true)
    }

    @Test
    @Throws(Exception::class)
    fun testApplyIsRefreshFalse() {
        testApply(false)
    }

    @Throws(Exception::class)
    private fun testApply(isRefresh: Boolean) {
        getTarget(isRefresh).apply(mockObservable!!)

        val errorTokenCaptor = ArgumentCaptor.forClass(CacheToken::class.java)
        val cacheTokenCaptor = ArgumentCaptor.forClass(CacheToken::class.java)

        verify<Any>(mockErrorInterceptorFactory).create(errorTokenCaptor.capture())
        verify<Any>(mockCacheInterceptorFactory).create(cacheTokenCaptor.capture(), any(Function::class.java) as Function<ApiError, Boolean>)

        val errorToken = errorTokenCaptor.value
        val cacheToken = cacheTokenCaptor.value

        assertEquals(errorToken, cacheToken)

        CacheTokenHelper.verifyCacheToken(
                errorToken,
                mockUrl,
                mockBody,
                "4529567321185990290",
                null!!,
                null!!,
                null!!, null!!,
                TestResponse::class.java,
                if (isRefresh) REFRESH else CacheInstruction.Operation.Type.CACHE,
                5f
        )
    }
}