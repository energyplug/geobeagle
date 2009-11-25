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

package com.google.code.geobeagle.actions;

import com.google.code.geobeagle.Geocache;
import com.google.code.geobeagle.R;
import com.google.code.geobeagle.activity.main.GeoBeagle;
import com.google.code.geobeagle.activity.main.intents.GeocacheToUri;
import com.google.code.geobeagle.activity.main.intents.IntentFactory;

import android.content.Intent;

public class CacheActionViewUri implements CacheAction {
    private final GeoBeagle mGeoBeagle;
    private final GeocacheToUri mGeocacheToUri;
    private final IntentFactory mIntentFactory;

    public CacheActionViewUri(GeoBeagle geoBeagle, IntentFactory intentFactory,
            GeocacheToUri geocacheToUri) {
        mGeoBeagle = geoBeagle;
        mGeocacheToUri = geocacheToUri;
        mIntentFactory = intentFactory;
    }

    @Override
    public void act(Geocache cache) {
        mGeoBeagle.startActivity(mIntentFactory.createIntent(Intent.ACTION_VIEW, 
                mGeocacheToUri.convert(cache)));
    }

    @Override
    public String getLabel(Geocache geocache) {
        return mGeoBeagle.getResources().getString(R.string.cache_page);
    }
}
