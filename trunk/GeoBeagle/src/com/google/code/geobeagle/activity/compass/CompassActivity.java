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

package com.google.code.geobeagle.activity.compass;

import com.google.code.geobeagle.ErrorDisplayer;
import com.google.code.geobeagle.Geocache;
import com.google.code.geobeagle.LocationControlBuffered;
import com.google.code.geobeagle.OnClickCancelListener;
import com.google.code.geobeagle.R;
import com.google.code.geobeagle.R.id;
import com.google.code.geobeagle.activity.compass.fieldnotes.DialogHelperSms;
import com.google.code.geobeagle.activity.compass.fieldnotes.DialogHelperSmsFactory;
import com.google.code.geobeagle.activity.compass.fieldnotes.FieldnoteLogger;
import com.google.code.geobeagle.activity.compass.fieldnotes.FieldnoteLoggerFactory;
import com.google.code.geobeagle.activity.compass.fieldnotes.OnClickOkFactory;
import com.google.code.geobeagle.activity.compass.fieldnotes.FieldnoteLogger.OnClickOk;
import com.google.code.geobeagle.activity.compass.intents.IntentStarterGeo;
import com.google.code.geobeagle.activity.compass.view.OnClickListenerCacheDetails;
import com.google.code.geobeagle.activity.compass.view.OnClickListenerIntentStarter;
import com.google.code.geobeagle.activity.compass.view.OnClickListenerRadar;
import com.google.code.geobeagle.activity.map.GeoMapActivity;
import com.google.inject.Inject;
import com.google.inject.Injector;

import roboguice.activity.GuiceActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import java.text.DateFormat;
import java.util.Date;

public class CompassActivity extends GuiceActivity {
    private static final DateFormat mLocalDateFormat = DateFormat
            .getTimeInstance(DateFormat.MEDIUM);
    private CompassActivityDelegate compassActivityDelegate;
    
    @Inject
    LocationControlBuffered locationControlBuffered;

    public Geocache getGeocache() {
        return compassActivityDelegate.getGeocache();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("GeoBeagle", "GeoBeagle onCreate");

        Injector injector = getInjector();
        injector.getInstance(CompassFragtivityOnCreateHandler.class).onCreate(this);
        
        RadarView radarView = injector.getInstance(RadarView.class);

        locationControlBuffered.onLocationChanged(null);

        LocationManager locationManager = injector.getInstance(LocationManager.class);

        // Register for location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, radarView);
        locationManager
                .requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, radarView);

        compassActivityDelegate = injector.getInstance(CompassActivityDelegate.class);

        // see http://www.androidguys.com/2008/11/07/rotational-forces-part-two/
        if (getLastNonConfigurationInstance() != null) {
            setIntent((Intent)getLastNonConfigurationInstance());
        }
        ErrorDisplayer errorDisplayer = injector.getInstance(ErrorDisplayer.class);
        Intent geoMapActivityIntent = new Intent(this, GeoMapActivity.class);
        OnClickListenerIntentStarter onClickListenerMapPage = new OnClickListenerIntentStarter(
                new IntentStarterGeo(this, geoMapActivityIntent), errorDisplayer);
        findViewById(id.maps).setOnClickListener(onClickListenerMapPage);

        findViewById(R.id.cache_details).setOnClickListener(
                injector.getInstance(OnClickListenerCacheDetails.class));
        OnClickListener onClickListenerNavigate = injector
                .getInstance(OnClickListenerNavigate.class);
        findViewById(id.navigate).setOnClickListener(onClickListenerNavigate);

        findViewById(id.radarview).setOnClickListener(
                injector.getInstance(OnClickListenerRadar.class));

        findViewById(id.menu_log_find).setOnClickListener(
                new LogFindClickListener(this, id.menu_log_find));
        findViewById(id.menu_log_dnf).setOnClickListener(
                new LogFindClickListener(this, id.menu_log_dnf));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return compassActivityDelegate.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (compassActivityDelegate.onKeyDown(keyCode, event))
            return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return compassActivityDelegate.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        Log.d("GeoBeagle", "GeoBeagle onPause");
        compassActivityDelegate.onPause();
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return compassActivityDelegate.onPrepareOptionsMenu(menu);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        return getIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CompassActivityDelegate.ACTIVITY_REQUEST_TAKE_PICTURE) {
            Log.d("GeoBeagle", "camera intent has returned.");
        } else if (resultCode == Activity.RESULT_OK)
            setIntent(data);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        super.onCreateDialog(id);
        Injector injector = getInjector();

        AlertDialog.Builder builder = injector.getInstance(AlertDialog.Builder.class);
        View fieldnoteDialogView = LayoutInflater.from(this).inflate(R.layout.fieldnote, null);

        boolean fDnf = id == R.id.menu_log_dnf;

        OnClickOk onClickOk = injector.getInstance(OnClickOkFactory.class).create(
                (EditText)fieldnoteDialogView.findViewById(R.id.fieldnote), fDnf);
        builder.setTitle(R.string.field_note_title);
        builder.setView(fieldnoteDialogView);
        builder.setNegativeButton(R.string.cancel,
                injector.getInstance(OnClickCancelListener.class));
        builder.setPositiveButton(R.string.log_cache, onClickOk);
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onCreateDialog(id);
        Injector injector = getInjector();

        boolean fDnf = id == R.id.menu_log_dnf;

        DialogHelperSms dialogHelperSms = injector.getInstance(DialogHelperSmsFactory.class)
                .create(compassActivityDelegate.getGeocache().getId().length(), fDnf);
        FieldnoteLogger fieldnoteLogger = injector.getInstance(FieldnoteLoggerFactory.class)
                .create(dialogHelperSms);

        fieldnoteLogger.onPrepareDialog(dialog, mLocalDateFormat.format(new Date()), fDnf);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        compassActivityDelegate.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("GeoBeagle", "GeoBeagle onResume");
        compassActivityDelegate.onResume();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        compassActivityDelegate.onSaveInstanceState(outState);
    }
}
