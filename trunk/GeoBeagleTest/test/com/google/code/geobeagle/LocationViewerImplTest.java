
package com.google.code.geobeagle;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import android.location.Location;
import android.location.LocationProvider;

import junit.framework.TestCase;

public class LocationViewerImplTest extends TestCase {

    public final void testCreate() {
        Location location = createMock(Location.class);
        MockableContext context = createMock(MockableContext.class);
        expect(location.getLatitude()).andReturn(37.0);
        expect(location.getLongitude()).andReturn(122.0);
        expect(location.getTime()).andReturn(300L);
        expect(location.getAccuracy()).andReturn(40.0f);
        MockableTextView lastUpdateTime = createMock(MockableTextView.class);
        lastUpdateTime.setText("16:00:00");
        MockableTextView coordinates = createMock(MockableTextView.class);
        coordinates.setText("37 00.000 122 00.000  �40.0m");

        replay(context);
        replay(location);
        replay(lastUpdateTime);
        replay(coordinates);
        new LocationViewerImpl(context, coordinates, lastUpdateTime, null, location);
        verify(context);
        verify(location);
        verify(lastUpdateTime);
        verify(coordinates);
    }

    public final void testSetStatus() {
        MockableContext context = createMock(MockableContext.class);
        expect(context.getString(R.string.out_of_service)).andReturn("OUT OF SERVICE");
        expect(context.getString(R.string.available)).andReturn("AVAILABLE");
        expect(context.getString(R.string.temporarily_unavailable)).andReturn(
                "TEMPORARILY UNAVAILABLE");
        MockableTextView lastUpdateTime = createMock(MockableTextView.class);
        MockableTextView coordinates = createMock(MockableTextView.class);
        coordinates.setText(R.string.getting_location_from_gps);
        MockableTextView status = createMock(MockableTextView.class);
        status.setText("OUT OF SERVICE");
        status.setText("AVAILABLE");
        status.setText("TEMPORARILY UNAVAILABLE");

        replay(context);
        replay(lastUpdateTime);
        replay(coordinates);
        replay(status);
        LocationViewer lv = new LocationViewerImpl(context, coordinates, lastUpdateTime, status,
                null);
        lv.setStatus(LocationProvider.OUT_OF_SERVICE);
        lv.setStatus(LocationProvider.AVAILABLE);
        lv.setStatus(LocationProvider.TEMPORARILY_UNAVAILABLE);
        verify(context);
        verify(lastUpdateTime);
        verify(coordinates);
        verify(status);
    }
}
