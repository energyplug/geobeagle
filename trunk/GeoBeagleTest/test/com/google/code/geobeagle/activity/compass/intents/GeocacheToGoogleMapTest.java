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

package com.google.code.geobeagle.activity.compass.intents;

import static org.junit.Assert.assertEquals;

import com.google.code.geobeagle.Geocache;
import com.google.code.geobeagle.R;
import com.google.code.geobeagle.activity.compass.intents.GeocacheToGoogleGeo;
import com.google.inject.Provider;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import android.content.res.Resources;
import android.text.SpannableString;

import java.util.Locale;

@PrepareForTest( {
        SpannableString.class
})
@RunWith(PowerMockRunner.class)
public class GeocacheToGoogleMapTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testConvert() {
        Locale.setDefault(Locale.GERMANY);

        Geocache geocache = PowerMock.createMock(Geocache.class);
        SpannableString ss = PowerMock.createMock(SpannableString.class);
        Provider<Resources> resourcesProvider = PowerMock.createMock(Provider.class);
        Resources resources = PowerMock.createMock(Resources.class);

        EasyMock.expect(geocache.getIdAndName()).andReturn(ss);
        EasyMock.expect(ss.toString()).andReturn("GCFOO pb & j(1.5/3)");
        EasyMock.expect(resourcesProvider.get()).andReturn(resources);
        EasyMock.expect(geocache.getLatitude()).andReturn(37.123);
        EasyMock.expect(geocache.getLongitude()).andReturn(122.345);
        EasyMock.expect(resources.getString(R.string.google_maps_intent)).andReturn(
                "geo:0,0?q=%1$.5f,%2$.5f (%3$s)");

        PowerMock.replayAll();
        GeocacheToGoogleGeo geocacheToCachePage = new GeocacheToGoogleGeo(resourcesProvider,
                R.string.google_maps_intent);
        assertEquals("geo:0,0?q=37.12300,122.34500 (GCFOO+pb+%26+j%5B1.5%2F3%5D)",
                geocacheToCachePage.convert(geocache));
        PowerMock.verifyAll();
    }
}
