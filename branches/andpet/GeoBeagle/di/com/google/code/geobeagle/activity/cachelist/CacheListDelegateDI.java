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

package com.google.code.geobeagle.activity.cachelist;

import com.google.code.geobeagle.CacheTypeFactory;
import com.google.code.geobeagle.ErrorDisplayer;
import com.google.code.geobeagle.GeoFixProvider;
import com.google.code.geobeagle.GeocacheFactory;
import com.google.code.geobeagle.GraphicsGenerator;
import com.google.code.geobeagle.IPausable;
import com.google.code.geobeagle.LocationControlDi;
import com.google.code.geobeagle.actions.CacheAction;
import com.google.code.geobeagle.actions.CacheActionConfirm;
import com.google.code.geobeagle.actions.CacheActionDelete;
import com.google.code.geobeagle.actions.CacheActionEdit;
import com.google.code.geobeagle.actions.CacheActionToggleFavorite;
import com.google.code.geobeagle.actions.CacheActionView;
import com.google.code.geobeagle.actions.CacheFilterUpdater;
import com.google.code.geobeagle.actions.MenuActionEditFilter;
import com.google.code.geobeagle.actions.MenuActionFilterListPopup;
import com.google.code.geobeagle.actions.MenuActionMap;
import com.google.code.geobeagle.actions.MenuActionSearchOnline;
import com.google.code.geobeagle.actions.MenuActionSettings;
import com.google.code.geobeagle.actions.MenuActions;
import com.google.code.geobeagle.activity.ActivityDI;
import com.google.code.geobeagle.activity.ActivitySaver;
import com.google.code.geobeagle.activity.cachelist.CacheListDelegate.ImportIntentManager;
import com.google.code.geobeagle.activity.cachelist.GeocacheListController.CacheListOnCreateContextMenuListener;
import com.google.code.geobeagle.activity.cachelist.actions.Abortable;
import com.google.code.geobeagle.activity.cachelist.actions.MenuActionMyLocation;
import com.google.code.geobeagle.activity.cachelist.actions.MenuActionSyncGpx;
import com.google.code.geobeagle.activity.cachelist.actions.MenuActionToggleFilter;
import com.google.code.geobeagle.activity.cachelist.presenter.BearingFormatter;
import com.google.code.geobeagle.activity.cachelist.presenter.CacheListAdapter;
import com.google.code.geobeagle.activity.cachelist.presenter.CacheListPositionUpdater;
import com.google.code.geobeagle.activity.cachelist.presenter.DistanceFormatterManager;
import com.google.code.geobeagle.activity.cachelist.presenter.DistanceFormatterManagerDi;
import com.google.code.geobeagle.activity.cachelist.presenter.GeocacheSummaryRowInflater;
import com.google.code.geobeagle.activity.cachelist.presenter.RelativeBearingFormatter;
import com.google.code.geobeagle.activity.cachelist.presenter.TitleUpdater;
import com.google.code.geobeagle.activity.filterlist.FilterTypeCollection;
import com.google.code.geobeagle.database.CachesProviderCenterThread;
import com.google.code.geobeagle.database.CachesProviderDb;
import com.google.code.geobeagle.database.CachesProviderCount;
import com.google.code.geobeagle.database.CachesProviderSorted;
import com.google.code.geobeagle.database.CachesProviderToggler;
import com.google.code.geobeagle.database.CachesProviderWaitForInit;
import com.google.code.geobeagle.database.DbFrontend;
import com.google.code.geobeagle.database.ICachesProviderCenter;
import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidget;
import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidgetDelegate;
import com.google.code.geobeagle.gpsstatuswidget.GpsWidgetAndUpdater;
import com.google.code.geobeagle.gpsstatuswidget.UpdateGpsWidgetRunnable;
import com.google.code.geobeagle.gpsstatuswidget.GpsStatusWidget.InflatedGpsStatusWidget;
import com.google.code.geobeagle.xmlimport.GpxImporterDI.MessageHandler;
import com.google.code.geobeagle.xmlimport.GpxToCache.Aborter;
import com.google.code.geobeagle.xmlimport.GpxToCacheDI.XmlPullParserWrapper;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout.LayoutParams;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CacheListDelegateDI {
    public static class Timing {
        private long mStartTime;

        public void lap(CharSequence msg) {
            long finishTime = Calendar.getInstance().getTimeInMillis();
            Log.d("GeoBeagle", "****** " + msg + ": " + (finishTime - mStartTime));
            mStartTime = finishTime;
        }

        public void start() {
            mStartTime = Calendar.getInstance().getTimeInMillis();
        }

        public long getTime() {
            return Calendar.getInstance().getTimeInMillis();
        }
    }

    public static CacheListDelegate create(ListActivity listActivity,
            LayoutInflater layoutInflater) {
        final OnClickListener onClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        };
        final ErrorDisplayer errorDisplayer = new ErrorDisplayer(listActivity, onClickListener);
        final GeoFixProvider geoFixProvider = LocationControlDi.create(listActivity);
        final GeocacheFactory geocacheFactory = new GeocacheFactory();
        final BearingFormatter relativeBearingFormatter = new RelativeBearingFormatter();
        final DistanceFormatterManager distanceFormatterManager = DistanceFormatterManagerDi
                .create(listActivity);
        final XmlPullParserWrapper xmlPullParserWrapper = new XmlPullParserWrapper();

        final GraphicsGenerator graphicsGenerator = new GraphicsGenerator();
        final GeocacheSummaryRowInflater geocacheSummaryRowInflater = new GeocacheSummaryRowInflater(
                distanceFormatterManager.getFormatter(), layoutInflater,
                relativeBearingFormatter, listActivity.getResources(), graphicsGenerator);

        final InflatedGpsStatusWidget inflatedGpsStatusWidget = new InflatedGpsStatusWidget(
                listActivity);
        final GpsStatusWidget gpsStatusWidget = new GpsStatusWidget(listActivity);

        gpsStatusWidget.addView(inflatedGpsStatusWidget, LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        final GpsWidgetAndUpdater gpsWidgetAndUpdater = new GpsWidgetAndUpdater(listActivity,
                gpsStatusWidget, geoFixProvider,
                distanceFormatterManager.getFormatter());
        final GpsStatusWidgetDelegate gpsStatusWidgetDelegate = gpsWidgetAndUpdater
                .getGpsStatusWidgetDelegate();

        inflatedGpsStatusWidget.setDelegate(gpsStatusWidgetDelegate);

        final UpdateGpsWidgetRunnable updateGpsWidgetRunnable = gpsWidgetAndUpdater
                .getUpdateGpsWidgetRunnable();
        updateGpsWidgetRunnable.run();
        
        final FilterTypeCollection filterTypeCollection = new FilterTypeCollection(listActivity);
        
        final DbFrontend dbFrontend = new DbFrontend(listActivity, geocacheFactory);
        final CachesProviderDb cachesProviderDb = new CachesProviderDb(dbFrontend);
        final ICachesProviderCenter cachesProviderCount = new CachesProviderWaitForInit(new CachesProviderCount(cachesProviderDb, 15, 30));
        final CachesProviderSorted cachesProviderSorted = new CachesProviderSorted(cachesProviderCount);
        //final CachesProviderLazy cachesProviderLazy = new CachesProviderLazy(cachesProviderSorted, 0.01, 2000, clock);
        ICachesProviderCenter cachesProviderLazy = cachesProviderSorted;

        final CachesProviderDb cachesProviderAll = new CachesProviderDb(dbFrontend);
        final CachesProviderToggler cachesProviderToggler = 
            new CachesProviderToggler(cachesProviderLazy, cachesProviderAll);
        CachesProviderCenterThread thread = new CachesProviderCenterThread(cachesProviderToggler);
        final TitleUpdater titleUpdater = new TitleUpdater(listActivity, 
                cachesProviderToggler, dbFrontend);

        distanceFormatterManager.addHasDistanceFormatter(geocacheSummaryRowInflater);
        distanceFormatterManager.addHasDistanceFormatter(gpsStatusWidgetDelegate);
        final CacheListAdapter cacheListAdapter = new CacheListAdapter(cachesProviderToggler, 
                cachesProviderSorted, geocacheSummaryRowInflater, titleUpdater, null);
        final CacheListPositionUpdater cacheListPositionUpdater = new CacheListPositionUpdater(
                geoFixProvider, cacheListAdapter, cachesProviderCount, thread /*cachesProviderSorted*/);
        geoFixProvider.addObserver(cacheListPositionUpdater);
        final CacheListAdapter.ScrollListener scrollListener = new CacheListAdapter.ScrollListener(
                cacheListAdapter);
        final CacheTypeFactory cacheTypeFactory = new CacheTypeFactory();

        final Aborter aborter = new Aborter();
        final MessageHandler messageHandler = MessageHandler.create(listActivity);
        //final CachePersisterFacadeFactory cachePersisterFacadeFactory = new CachePersisterFacadeFactory(
        //        messageHandler, cacheTypeFactory);

        final GpxImporterFactory gpxImporterFactory = new GpxImporterFactory(aborter,
                errorDisplayer, geoFixProvider, listActivity,
                messageHandler, xmlPullParserWrapper, cacheTypeFactory);

        final Abortable nullAbortable = new Abortable() {
            public void abort() {
            }
        };

        // *** BUILD MENU ***
        final Resources resources = listActivity.getResources();
        final MenuActionSyncGpx menuActionSyncGpx = new MenuActionSyncGpx(nullAbortable,
                cacheListAdapter, gpxImporterFactory, dbFrontend, resources);
        final CacheActionEdit cacheActionEdit = new CacheActionEdit(listActivity);
        final MenuActions menuActions = new MenuActions();
        menuActions.add(new MenuActionToggleFilter(cachesProviderToggler, cacheListAdapter, resources));
        menuActions.add(new MenuActionSearchOnline(listActivity));
        List<CachesProviderDb> providers = new ArrayList<CachesProviderDb>();
        providers.add(cachesProviderDb);
        providers.add(cachesProviderAll);
        final CacheFilterUpdater cacheFilterUpdater = 
            new CacheFilterUpdater(filterTypeCollection, providers);
        menuActions.add(new MenuActionMap(listActivity, geoFixProvider));
        //menuActions.add(new MenuActionFilterList(listActivity));
        menuActions.add(new MenuActionEditFilter(listActivity, cacheFilterUpdater, 
                cacheListAdapter, filterTypeCollection));
        menuActions.add(new MenuActionFilterListPopup(listActivity, cacheFilterUpdater, 
                cacheListAdapter, filterTypeCollection));
        menuActions.add(new MenuActionMyLocation(errorDisplayer,
                geocacheFactory, geoFixProvider, dbFrontend, resources, cacheActionEdit));
        menuActions.add(menuActionSyncGpx);
        menuActions.add(new MenuActionSettings(listActivity));
        
        // *** BUILD CONTEXT MENU ***
        final CacheActionView cacheActionView = new CacheActionView(listActivity);
        final CacheActionToggleFavorite cacheActionToggleFavorite = 
            new CacheActionToggleFavorite(dbFrontend, cacheListAdapter, cacheFilterUpdater);
        //TODO: It is currently a bug to send cachesProviderDb since cachesProviderAll also need to be notified of db changes.
        final CacheActionDelete cacheActionDelete = 
            new CacheActionDelete(cacheListAdapter, titleUpdater, dbFrontend, cachesProviderDb, resources);
        
        final AlertDialog.Builder builder = new AlertDialog.Builder(listActivity);
        final CacheActionConfirm cacheActionConfirmDelete =
            new CacheActionConfirm(listActivity, builder, cacheActionDelete);
        
        final CacheAction[] contextActions = new CacheAction[] {
                cacheActionView, cacheActionToggleFavorite, 
                cacheActionEdit, cacheActionConfirmDelete
        };
        final GeocacheListController geocacheListController = 
            new GeocacheListController(cacheListAdapter, contextActions, menuActionSyncGpx, 
                    menuActions, cacheActionView, cacheFilterUpdater);

        final ActivitySaver activitySaver = ActivityDI.createActivitySaver(listActivity);
        final ImportIntentManager importIntentManager = new ImportIntentManager(listActivity);
        final CacheListOnCreateContextMenuListener menuCreator = 
            new CacheListOnCreateContextMenuListener(cachesProviderToggler, contextActions);
        final IPausable pausables[] = { geoFixProvider, thread };

        //TODO: It is currently a bug to send cachesProviderDb since cachesProviderAll also need to be notified of db changes.
        return new CacheListDelegate(importIntentManager, activitySaver,
                geocacheListController, dbFrontend, updateGpsWidgetRunnable, 
                gpsStatusWidget, menuCreator, cacheListAdapter, geocacheSummaryRowInflater, listActivity, 
                scrollListener, distanceFormatterManager, cachesProviderDb, pausables);
    }
}
