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

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.code.geobeagle.Geocache;
import com.google.code.geobeagle.activity.PausableWithDatabase;
import com.google.code.geobeagle.database.DatabaseDI;
import com.google.code.geobeagle.database.DatabaseDI.GeoBeagleSqliteOpenHelper;

/** Will develop to represent the front-end to access a database.
 * It takes responsibility to open and close the actual database connection
 * without involving the clients of this class. 
 */
public class DbFrontend implements PausableWithDatabase {
	CacheReader mCacheReader;
	Context mContext;
	GeoBeagleSqliteOpenHelper open;
	boolean mIsDatabaseOpen;
	CacheWriter mCacheWriter;
	ISQLiteDatabase mDatabase;
	
	public DbFrontend(Context context) {
		mContext = context;
		mIsDatabaseOpen = false;
	}

	public void openDatabase() {
		if (mIsDatabaseOpen)
			return;
		Log.d("GeoBeagle", "DbFrontend.openDatabase()");
		mIsDatabaseOpen = true;
		
        open = new GeoBeagleSqliteOpenHelper(mContext);
        final SQLiteDatabase sqDb = open.getReadableDatabase();
        mDatabase = new DatabaseDI.SQLiteWrapper(sqDb);
		
        mCacheReader = DatabaseDI.createCacheReader(mDatabase);
	}
	
	public void closeDatabase() {
		if (!mIsDatabaseOpen)
			return;
		Log.d("GeoBeagle", "DbFrontend.closeDatabase()");
		mIsDatabaseOpen = false;
		
        open.close();
		mCacheWriter = null;
		mDatabase = null;
	}

    public ArrayList<Geocache> loadCaches(double latitude, double longitude, 
                                          WhereFactory whereFactory) {
    	openDatabase();
    	
    	CacheReaderCursor cursor = mCacheReader.open(latitude, longitude, whereFactory);
    	ArrayList<Geocache> geocaches = new ArrayList<Geocache>();
    	if (cursor != null) {
        	do {
        		geocaches.add(cursor.getCache());
        	} while (cursor.moveToNext());
    		cursor.close();
    	}
    	return geocaches;
    }
    
    public CacheWriter getCacheWriter() {
        if (mCacheWriter != null)
            return mCacheWriter;
        openDatabase();
        mCacheWriter = DatabaseDI.createCacheWriter(mDatabase);
        return mCacheWriter;
    }
    
    @Override
    public void onPause() {
        closeDatabase();
    }

    @Override
    public void onResume(ISQLiteDatabase sqliteDatabase) {
        //TODO: Should anything be done up-front or is lazy just as good?
        //openDatabase();
    }
}
