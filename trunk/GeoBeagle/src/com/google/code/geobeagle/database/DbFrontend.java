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

package com.google.code.geobeagle.database;

import com.google.code.geobeagle.Geocache;
import com.google.code.geobeagle.cachedetails.DetailsDatabaseWriter;
import com.google.code.geobeagle.database.DatabaseDI.GeoBeagleSqliteOpenHelper;
import com.google.code.geobeagle.preferences.PreferencesUpgrader;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

/**
 * Will develop to represent the front-end to access a database. It takes
 * responsibility to mSqliteOpenHelper and close the actual database connection
 * without involving the clients of this class.
 */
@Singleton
public class DbFrontend {
    private final CacheReader mCacheReader;
    private final Context mContext;
    private ISQLiteDatabase mDatabase;
    private GeoBeagleSqliteOpenHelper mSqliteOpenHelper;
    private final PreferencesUpgrader mPreferencesUpgrader;
    private final DetailsDatabaseWriter mSdDatabase;

    @Inject
    DbFrontend(Activity activity,
            CacheReader cacheReader,
            PreferencesUpgrader preferencesUpgrader,
            DetailsDatabaseWriter detailsDatabaseWriter) {
        mCacheReader = cacheReader;
        mPreferencesUpgrader = preferencesUpgrader;
        mContext = activity.getApplicationContext();
        mSdDatabase = detailsDatabaseWriter;
    }

    public synchronized void closeDatabase() {
        Log.d("GeoBeagleDb", this + ": DbFrontend.closeDatabase() " + mContext);
        if (mContext == null || mSqliteOpenHelper == null)
            return;
        mSqliteOpenHelper.close();
        mDatabase = null;
        mSqliteOpenHelper = null;
    }

    public int count(int latitude, int longitude, WhereFactoryFixedArea whereFactory) {
        openDatabase();
        Cursor countCursor = mDatabase.rawQuery("SELECT COUNT(*) FROM " + Database.TBL_CACHES
                + " WHERE " + whereFactory.getWhere(mDatabase, latitude, longitude), null);
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        countCursor.close();

        Log.d("GeoBeagle", this + ": DbFrontEnd.count:" + count);
        return count;
    }

    public int countAll() {
        openDatabase();
        Cursor countCursor = mDatabase
                .rawQuery("SELECT COUNT(*) FROM " + Database.TBL_CACHES, null);
        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        countCursor.close();
        Log.d("GeoBeagle", this + ": DbFrontEnd.count all:" + count);
        return count;
    }

    public ArrayList<Geocache> loadCaches(double latitude, double longitude,
            WhereFactory whereFactory) {
        Log.d("GeoBeagle", "DbFrontend.loadCaches " + latitude + ", " + longitude);
        openDatabase();

        CacheReaderCursor cursor = mCacheReader.open(latitude, longitude, whereFactory, null);
        ArrayList<Geocache> geocaches = new ArrayList<Geocache>();
        if (cursor != null) {
            do {
                geocaches.add(cursor.getCache());
            } while (cursor.moveToNext());
            cursor.close();
        }
        return geocaches;
    }

    public synchronized void openDatabase() {
        if (mSqliteOpenHelper != null)
            return;

        Log.d("GeoBeagleDb", this + ": DbFrontend.openDatabase() " + mContext);
        mSqliteOpenHelper = new GeoBeagleSqliteOpenHelper(mContext, mPreferencesUpgrader);
        mDatabase = new DatabaseDI.SQLiteWrapper(mSqliteOpenHelper.getWritableDatabase());
    }

    public Geocache getCache(CharSequence cacheId) {
        CacheReaderCursor cacheReader = mCacheReader.open(cacheId);
        Geocache cache = cacheReader.getCache();
        cacheReader.close();
        return cache;
    }

    public void deleteAll() {
        openDatabase();
        mDatabase.execSQL(Database.SQL_DELETE_ALL_CACHES);
        mDatabase.execSQL(Database.SQL_DELETE_ALL_GPX);
        mSdDatabase.deleteAll();
    }

    public void forceUpdate() {
        openDatabase();
        mDatabase.execSQL(Database.SQL_FORCE_UPDATE_ALL);
    }
    public ISQLiteDatabase getDatabase() {
        openDatabase();
        return mDatabase;
    }

    /*
     * public void onPause() { closeDatabase(); }
     */

    /*
     * public void onResume() { //Lazy evaluation - mSqliteOpenHelper database
     * when needed }
     */
}
