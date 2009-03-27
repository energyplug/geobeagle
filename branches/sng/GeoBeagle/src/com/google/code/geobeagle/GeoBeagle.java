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

package com.google.code.geobeagle;

import com.google.code.geobeagle.LocationControl.LocationChooser;
import com.google.code.geobeagle.data.Geocache;
import com.google.code.geobeagle.data.GeocacheFromPreferencesFactory;
import com.google.code.geobeagle.data.di.GeocacheFactory;
import com.google.code.geobeagle.intents.GeocacheToCachePage;
import com.google.code.geobeagle.intents.GeocacheToGoogleMap;
import com.google.code.geobeagle.intents.IntentFactory;
import com.google.code.geobeagle.intents.IntentStarterLocation;
import com.google.code.geobeagle.intents.IntentStarterRadar;
import com.google.code.geobeagle.intents.IntentStarterViewUri;
import com.google.code.geobeagle.ui.CacheListDelegate;
import com.google.code.geobeagle.ui.CachePageButtonEnabler;
import com.google.code.geobeagle.ui.ContentSelector;
import com.google.code.geobeagle.ui.ErrorDisplayer;
import com.google.code.geobeagle.ui.GeocacheListOnClickListener;
import com.google.code.geobeagle.ui.GeocacheViewer;
import com.google.code.geobeagle.ui.GetCoordsToast;
import com.google.code.geobeagle.ui.GpsStatusWidget;
import com.google.code.geobeagle.ui.MockableTextView;
import com.google.code.geobeagle.ui.MyLocationProvider;
import com.google.code.geobeagle.ui.OnCacheButtonClickListenerBuilder;
import com.google.code.geobeagle.ui.OnContentProviderSelectedListener;
import com.google.code.geobeagle.ui.GpsStatusWidget.MeterFormatter;
import com.google.code.geobeagle.ui.GpsStatusWidget.MeterView;
import com.google.code.geobeagle.ui.di.EditCacheActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

/*
 * Main Activity for GeoBeagle.
 */
public class GeoBeagle extends Activity implements LifecycleManager {
    private CachePageButtonEnabler mCachePageButtonEnabler;
    private ContentSelector mContentSelector;
    private final ErrorDisplayer mErrorDisplayer;
    private GeoBeagleDelegate mGeoBeagleDelegate;
    private Geocache mGeocache;
    private GeocacheFromPreferencesFactory mGeocacheFromPreferencesFactory;
    private GeocacheViewer mGeocacheViewer;
    private LocationControl mGpsControl;
    private final Handler mHandler;
    private GeoBeagleLocationListener mLocationListener;
    private GpsStatusWidget mLocationViewer;

    private final ResourceProvider mResourceProvider;
    private final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            mLocationViewer.refreshLocation();
            mHandler.postDelayed(mUpdateTimeTask, 100);
        }
    };

    public GeoBeagle() {
        super();
        mErrorDisplayer = new ErrorDisplayer(this);
        mResourceProvider = new ResourceProvider(this);
        mHandler = new Handler();
    }

    private MockableTextView createTextView(int id) {
        return new MockableTextView((TextView)findViewById(id));
    }

    private void getCoordinatesFromIntent(GeocacheViewer geocacheViewer, Intent intent,
            ErrorDisplayer errorDisplayer) {
        try {
            if (intent.getType() == null) {
                final String query = intent.getData().getQuery();
                final CharSequence sanitizedQuery = Util.parseHttpUri(query,
                        new UrlQuerySanitizer(), UrlQuerySanitizer
                                .getAllButNulAndAngleBracketsLegal());
                final CharSequence[] latlon = Util.splitLatLonDescription(sanitizedQuery);
                mGeocache = new Geocache(latlon[2], "", Util.parseCoordinate(latlon[0]), Util
                        .parseCoordinate(latlon[1]));
                geocacheViewer.set(mGeocache);
            }
        } catch (final Exception e) {
            errorDisplayer.displayError("Error: " + e.getMessage());
        }
    }

    public Geocache getGeocache() {
        return mGeocache;
    }

    private boolean maybeGetCoordinatesFromIntent() {
        final Intent intent = getIntent();
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(Intent.ACTION_VIEW)) {
                    getCoordinatesFromIntent(mGeocacheViewer, intent, mErrorDisplayer);
                    return true;
                } else if (action.equals(CacheListDelegate.SELECT_CACHE)) {
                    mGeocache = intent.<Geocache> getParcelableExtra("geocache");
                    mGeocacheViewer.set(mGeocache);
                    mCachePageButtonEnabler.check();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0)
            setIntent(data);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            GeoBeagleBuilder builder = new GeoBeagleBuilder(this);
            mContentSelector = builder.createContentSelector(getPreferences(Activity.MODE_PRIVATE));

            final TextView txtLocation = (TextView)findViewById(R.id.go_to);
            mCachePageButtonEnabler = CachePageButtonEnabler.create(txtLocation,
                    findViewById(R.id.cache_page), findViewById(R.id.cache_details),
                    mResourceProvider);

            mLocationViewer = new GpsStatusWidget(mResourceProvider, new MeterView(
                    createTextView(R.id.location_viewer), new MeterFormatter()),
                    createTextView(R.id.provider), createTextView(R.id.lag),
                    createTextView(R.id.accuracy), createTextView(R.id.status),
                    new GpsStatusWidget.Time(), new Location(""));
            final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            mGpsControl = new LocationControl(locationManager, new LocationChooser());
            mLocationListener = new GeoBeagleLocationListener(mGpsControl, mLocationViewer);
            final GeocacheFactory geocacheFactory = new GeocacheFactory();
            mGeocacheFromPreferencesFactory = new GeocacheFromPreferencesFactory(geocacheFactory);
            mGeocacheViewer = new GeocacheViewer(txtLocation, mGeocacheFromPreferencesFactory);

            setCacheClickListeners();

            ((Button)findViewById(R.id.go_to_list))
                    .setOnClickListener(new GeocacheListOnClickListener(this));

            AppLifecycleManager appLifecycleManager = new AppLifecycleManager(
                    getPreferences(MODE_PRIVATE), new LifecycleManager[] {
                            this, new LocationLifecycleManager(mLocationListener, locationManager),
                            mContentSelector
                    });
            mGeoBeagleDelegate = GeoBeagleDelegate.buildGeoBeagleDelegate(this,
                    appLifecycleManager, mGeocacheViewer, mErrorDisplayer);
            mGeoBeagleDelegate.onCreate();

            ((Spinner)findViewById(R.id.content_provider))
                    .setOnItemSelectedListener(new OnContentProviderSelectedListener(
                            mResourceProvider, new MockableTextView(
                                    (TextView)findViewById(R.id.select_cache_prompt)),
                            new MockableTextView((TextView)findViewById(R.id.cache_prompt))));

            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.postDelayed(mUpdateTimeTask, 1000);
        } catch (final Exception e) {
            mErrorDisplayer.displayErrorAndStack(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.menu_edit_geocache);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, EditCacheActivity.class);
        intent.putExtra("geocache", mGeocache);
        startActivityForResult(intent, 0);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mGeoBeagleDelegate.onPause();
    }

    public void onPause(Editor editor) {
        getGeocache().writeToPrefs(editor);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGeoBeagleDelegate.onResume();
        maybeGetCoordinatesFromIntent();
        mCachePageButtonEnabler.check();
        final Location location = mGpsControl.getLocation();
        if (location != null)
            mLocationListener.onLocationChanged(location);
    }

    public void onResume(SharedPreferences preferences) {
        setGeocache(mGeocacheFromPreferencesFactory.create(preferences));
    }

    private void setCacheClickListeners() {
        IntentFactory intentFactory = new IntentFactory(new UriParser());
        GetCoordsToast getCoordsToast = new GetCoordsToast(this);
        MyLocationProvider myLocationProvider = new MyLocationProvider(mGpsControl, mErrorDisplayer);

        OnCacheButtonClickListenerBuilder cacheClickListenerSetter = new OnCacheButtonClickListenerBuilder(
                this, mErrorDisplayer);

        cacheClickListenerSetter.set(R.id.object_map, new IntentStarterLocation(this,
                mResourceProvider, intentFactory, myLocationProvider, mContentSelector,
                R.array.map_objects, getCoordsToast), "");
        cacheClickListenerSetter.set(R.id.nearest_objects, new IntentStarterLocation(this,
                mResourceProvider, intentFactory, myLocationProvider, mContentSelector,
                R.array.nearest_objects, getCoordsToast), "");

        cacheClickListenerSetter.set(R.id.maps, new IntentStarterViewUri(this, intentFactory,
                mGeocacheViewer, new GeocacheToGoogleMap(mResourceProvider)), "");
        cacheClickListenerSetter.set(R.id.cache_page, new IntentStarterViewUri(this, intentFactory,
                mGeocacheViewer, new GeocacheToCachePage(mResourceProvider, mContentSelector)), "");
        cacheClickListenerSetter.set(R.id.radar, new IntentStarterRadar(this, intentFactory),
                "\nPlease install the Radar application to use Radar.");
    }

    void setGeocache(Geocache geocache) {
        mGeocache = geocache;
        mGeocacheViewer.set(getGeocache());
    }
}
