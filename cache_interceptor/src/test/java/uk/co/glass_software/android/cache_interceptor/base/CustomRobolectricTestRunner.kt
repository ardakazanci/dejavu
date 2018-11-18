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

package uk.co.glass_software.android.cache_interceptor.base

import org.junit.runners.model.InitializationError
import org.robolectric.RobolectricTestRunner

import uk.co.glass_software.android.cache_interceptor.BuildConfig

class CustomRobolectricTestRunner
@Throws(InitializationError::class)
constructor(testClass: Class<*>)
    : RobolectricTestRunner(testClass) {

    init {
        System.setProperty("android.package", BuildConfig.APPLICATION_ID)
        val folder = BuildConfig.FLAVOR + "/" + BuildConfig.BUILD_TYPE
        System.setProperty("android.manifest",
                "build/intermediates/manifests/aapt/$folder/AndroidManifest.xml"
        )
        System.setProperty("android.resources",
                "build/intermediates/res/merged/$folder"
        )
        System.setProperty("android.assets", "build/intermediates/assets/$folder")
    }

}