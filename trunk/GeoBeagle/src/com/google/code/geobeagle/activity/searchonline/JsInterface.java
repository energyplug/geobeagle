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

package com.google.code.geobeagle.activity.searchonline;

import com.google.code.geobeagle.LocationControlBuffered;
import com.google.code.geobeagle.R;
import com.google.code.geobeagle.xmlimport.GpxImporterDI;
import com.google.code.geobeagle.xmlimport.GpxImporterDI.ToastFactory;
import com.google.inject.Inject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.widget.Toast;

import java.util.Locale;

class JsInterface {
    private final LocationControlBuffered mLocationControlBuffered;
    private final JsInterfaceHelper mHelper;
    private ToastFactory mToastFactory;
    private Context mContext;

    static class JsInterfaceHelper {
        private final Activity mActivity;

        @Inject
        public JsInterfaceHelper(Activity activity) {
            mActivity = activity;
        }

        public void launch(String uri) {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        }

        public String getTemplate(int ix) {
            return mActivity.getResources().getStringArray(R.array.nearest_objects)[ix];
        }

        public String getNS(double latitude) {
            return latitude > 0 ? "N" : "S";
        }

        public String getEW(double longitude) {
            return longitude > 0 ? "E" : "W";
        }
    }

    @Inject
    public JsInterface(LocationControlBuffered locationControlBuffered,
            JsInterfaceHelper jsInterfaceHelper,
            GpxImporterDI.ToastFactory toastFactory, Context context) {
        mHelper = jsInterfaceHelper;
        mLocationControlBuffered = locationControlBuffered;
        mToastFactory = toastFactory;
        mContext = context;
    }

    public int atlasQuestOrGroundspeak(int ix) {
        final Location location = mLocationControlBuffered.getLocation();
        if (location == null) {
            mToastFactory.showToast(mContext, R.string.current_location_null,
                    Toast.LENGTH_LONG);
            return 0;
        }
        final String uriTemplate = mHelper.getTemplate(ix);
        mHelper.launch(String.format(Locale.US, uriTemplate, location.getLatitude(), location
                .getLongitude()));
        return 0;
    }

    public int openCaching(int ix) {
        final Location location = mLocationControlBuffered.getLocation();
        if (location == null) {
            mToastFactory.showToast(mContext, R.string.current_location_null,
                    Toast.LENGTH_LONG);
            return 0;
        }
        final String uriTemplate = mHelper.getTemplate(ix);
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final String NS = mHelper.getNS(latitude);
        final String EW = mHelper.getEW(longitude);

        final double abs_latitude = Math.abs(latitude);
        final double abs_longitude = Math.abs(longitude);

        final int lat_h = (int)abs_latitude;
        final double lat_m = 60 * (abs_latitude - lat_h);
        final int lon_h = (int)abs_longitude;
        final double lon_m = 60 * (abs_longitude - lon_h);

        mHelper.launch(String.format(Locale.US, uriTemplate, NS, lat_h, lat_m, EW, lon_h, lon_m));
        return 0;
    }
}
