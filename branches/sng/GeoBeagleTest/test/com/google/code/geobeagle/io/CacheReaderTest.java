
package com.google.code.geobeagle.io;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import com.google.code.geobeagle.io.CacheReader.CacheReaderCursor;
import com.google.code.geobeagle.io.CacheReader.WhereFactory;
import com.google.code.geobeagle.io.di.DatabaseDI;
import com.google.code.geobeagle.io.di.DatabaseDI.SQLiteWrapper;

import android.database.Cursor;
import android.location.Location;

import junit.framework.TestCase;

public class CacheReaderTest extends TestCase {

    private void expectQuery(SQLiteWrapper sqliteWrapper, Cursor cursor, String where) {
        expect(
                sqliteWrapper.query(eq("CACHES"), eq(Database.READER_COLUMNS), eq(where),
                        (String)isNull(), (String)isNull(), (String)isNull(),
                        eq(CacheReader.SQL_QUERY_LIMIT))).andReturn(cursor);
    }

    public void testCursorClose() {
        Cursor cursor = createMock(Cursor.class);

        cursor.close();

        replay(cursor);
        new CacheReaderCursor(cursor).close();
        verify(cursor);
    }

    public void testCursorGetCache() {
        Cursor cursor = createMock(Cursor.class);

        expect(cursor.getString(0)).andReturn("122");
        expect(cursor.getString(1)).andReturn("37");
        expect(cursor.getString(2)).andReturn("the_name");
        expect(cursor.getString(3)).andReturn("description");

        replay(cursor);
        assertEquals("122, 37 (the_name: description)", new CacheReaderCursor(cursor).getCache());
        verify(cursor);
    }

    public void testCursorMoveToNext() {
        Cursor cursor = createMock(Cursor.class);

        expect(cursor.moveToNext()).andReturn(true);

        replay(cursor);
        new CacheReaderCursor(cursor).moveToNext();
        verify(cursor);
    }

    public void testGetTotalCount() {
        SQLiteWrapper sqliteWrapper = createMock(SQLiteWrapper.class);

        expect(sqliteWrapper.countResults(Database.TBL_CACHES, null)).andReturn(17);

        replay(sqliteWrapper);
        assertEquals(17, new CacheReader(sqliteWrapper, null, null).getTotalCount());
        verify(sqliteWrapper);
    }

    public void testGetWhere() {
        Location location = createMock(Location.class);
        expect(location.getLatitude()).andReturn(90.0);
        expect(location.getLongitude()).andReturn(180.0);

        replay(location);
        assertEquals(
                "Latitude > 89.92 AND Latitude < 90.08 AND Longitude > -180.0 AND Longitude < 180.0",
                new WhereFactory().getWhere(location));
        verify(location);
    }

    public void testGetWhereNullLocation() {
        assertEquals(null, new WhereFactory().getWhere(null));
    }

    public void testOpen() {
        Location location = createMock(Location.class);
        WhereFactory whereFactory = createMock(WhereFactory.class);
        SQLiteWrapper sqliteWrapper = createMock(SQLiteWrapper.class);
        Cursor cursor = createMock(Cursor.class);
        DatabaseDI.CacheReaderCursorFactory cacheReaderCursorFactory = createMock(DatabaseDI.CacheReaderCursorFactory.class);
        CacheReaderCursor cacheReaderCursor = createMock(CacheReaderCursor.class);

        String where = "Latitude > something AND Longitude < somethingelse";
        expect(whereFactory.getWhere(location)).andReturn(where);
        expectQuery(sqliteWrapper, cursor, where);
        expect(cursor.moveToFirst()).andReturn(true);
        expect(cacheReaderCursorFactory.create(cursor)).andReturn(cacheReaderCursor);

        replay(whereFactory);
        replay(sqliteWrapper);
        replay(cursor);
        replay(cacheReaderCursorFactory);
        assertEquals(cacheReaderCursor, new CacheReader(sqliteWrapper, whereFactory,
                cacheReaderCursorFactory).open(location));
        verify(whereFactory);
        verify(sqliteWrapper);
        verify(cursor);
        verify(cacheReaderCursorFactory);

    }

    public void testOpenEmpty() {
        SQLiteWrapper sqliteWrapper = createMock(SQLiteWrapper.class);
        Cursor cursor = createMock(Cursor.class);
        WhereFactory whereFactory = createMock(WhereFactory.class);

        expect(whereFactory.getWhere(null)).andReturn("a=b");
        expectQuery(sqliteWrapper, cursor, "a=b");
        expect(cursor.moveToFirst()).andReturn(false);
        cursor.close();

        replay(whereFactory);
        replay(sqliteWrapper);
        replay(cursor);
        new CacheReader(sqliteWrapper, whereFactory, null).open(null);
        verify(sqliteWrapper);
        verify(cursor);
        verify(whereFactory);
    }
}
