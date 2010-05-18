/*
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */
package com.google.code.geobeagle.activity.cachelist.model;

import com.google.inject.Provides;

import java.util.ArrayList;

import roboguice.config.AbstractAndroidModule;
import roboguice.inject.ContextScoped;

public class ModelModule extends AbstractAndroidModule {

    @Override
    protected void configure() {
    }

    @Provides
    @ContextScoped
    GeocacheVectors providesGeocacheVectors() {
        final ArrayList<GeocacheVector> geocacheVectorsList = new ArrayList<GeocacheVector>(10);
        return new GeocacheVectors(geocacheVectorsList);
    }
}