/*
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

package com.google.code.geobeagle.io;

import com.google.code.geobeagle.ui.ErrorDisplayer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseFactory {
    public static DatabaseFactory create(Context context) {
        return new DatabaseFactory(new SQLiteWrapper(), new GeoBeagleSqliteOpenHelper(context,
                new OpenHelperDelegate()));
    }

    public static class CacheReader {
        private Cursor mCursor;
        private final SQLiteDatabase mSqliteDatabase;
        private final SQLiteWrapper mSqliteWrapper;

        public CacheReader(SQLiteDatabase sqliteDatabase, SQLiteWrapper sqliteWrapper) {
            mSqliteDatabase = sqliteDatabase;
            mSqliteWrapper = sqliteWrapper;
        }

        public void close() {
            mCursor.close();
        }

        public String getCache() {
            String name = mCursor.getString(3);
            if (name.length() > 0) {
                name = ": " + name;
            }

            return mCursor.getString(0) + ", " + mCursor.getString(1) + " (" + mCursor.getString(2)
                    + name + ")";
        }

        public boolean moveToNext() {
            return mCursor.moveToNext();
        }

        public boolean open() {
            try {
                mCursor = mSqliteWrapper.query(mSqliteDatabase, "CACHES", READER_COLUMNS, null,
                        null, null, null, null);
            } catch (SQLiteException e) {
                e.printStackTrace();
                return false;
            }
            final boolean result = mCursor.moveToFirst();
            if (!result)
                mCursor.close();
            return result;
        }
    }

    public static class CacheWriter {
        private final ErrorDisplayer mErrorDisplayer;
        private final SQLiteDatabase mSqlite;

        public CacheWriter(SQLiteDatabase sqlite, ErrorDisplayer errorDisplayer) {
            mSqlite = sqlite;
            mErrorDisplayer = errorDisplayer;
        }

        public void clear() {
            mSqlite.execSQL("DELETE FROM CACHES");
        }

        public boolean write(CharSequence id, CharSequence name, double latitude, double longitude) {
            try {
                mSqlite.execSQL(DatabaseFactory.SQL_INSERT_CACHE_FULL, new Object[] {
                        name, id, new Double(latitude), new Double(longitude), "", "import"
                });
            } catch (final SQLiteException e) {
                mErrorDisplayer.displayError("Error writing cache: " + e.getMessage());
                return false;
            }
            return true;
        }
    }

    public static class OpenHelperDelegate {
        public void onCreate(SQLiteDatabase db) {
            // TODO: handle errors.
            db.execSQL(SQL_CREATE_CACHE_TABLE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP_CACHE_TABLE);
            onCreate(db);
        }
    }

    public static class GeoBeagleSqliteOpenHelper extends SQLiteOpenHelper {
        private final OpenHelperDelegate mOpenHelperDelegate;

        public GeoBeagleSqliteOpenHelper(Context context, OpenHelperDelegate openHelperDelegate) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mOpenHelperDelegate = openHelperDelegate;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mOpenHelperDelegate.onCreate(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mOpenHelperDelegate.onUpgrade(db, oldVersion, newVersion);
        }
    }

    public static class SQLiteWrapper {
        public Cursor query(SQLiteDatabase db, String table, String[] columns, String selection,
                String[] selectionArgs, String groupBy, String having, String orderBy) {
            return db.query(table, columns, selection, selectionArgs, groupBy, orderBy, having);
        }
    }

    public static final String DATABASE_NAME = "GeoBeagle.db";
    public static final int DATABASE_VERSION = 3;
    public static final String[] READER_COLUMNS = new String[] {
            "Latitude", "Longitude", "Name", "Description"
    };
    public static final String SQL_CREATE_CACHE_TABLE = "CREATE TABLE IF NOT EXISTS CACHES ("
            + "Id INTEGER PRIMARY KEY AUTOINCREMENT, Description VARCHAR, Name VARCHAR, Details VARCHAR, "
            + "Latitude DOUBLE, Longitude DOUBLE, Source VARCHAR)";
    public static final String SQL_DROP_CACHE_TABLE = "DROP TABLE CACHES";
    public static final String SQL_INSERT_CACHE_FULL = "INSERT INTO CACHES "
            + "(Description, Name, Latitude, Longitude, Details, Source) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private final SQLiteOpenHelper mSqliteOpenHelper;
    private final SQLiteWrapper mSqliteWrapper;

    public DatabaseFactory(SQLiteWrapper sqliteWrapper, SQLiteOpenHelper sqliteOpenHelper) {
        mSqliteWrapper = sqliteWrapper;
        mSqliteOpenHelper = sqliteOpenHelper;
    }

    public CacheReader createCacheReader(SQLiteDatabase sqlite) {
        return new CacheReader(sqlite, mSqliteWrapper);
    }

    public CacheWriter createCacheWriter(SQLiteDatabase sqlite, ErrorDisplayer errorDisplayer) {
        return new CacheWriter(sqlite, errorDisplayer);
    }

    public SQLiteDatabase openOrCreateCacheDatabase(ErrorDisplayer errorDisplayer) {
        try {
            return mSqliteOpenHelper.getWritableDatabase();
        } catch (final SQLiteException e) {
            errorDisplayer.displayError("Error opening or creating database " + DATABASE_NAME
                    + ": " + e.getMessage() + ".");
        }
        return null;
    }
}
