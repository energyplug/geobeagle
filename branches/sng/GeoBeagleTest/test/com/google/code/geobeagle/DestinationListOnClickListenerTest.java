
package com.google.code.geobeagle;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import com.google.code.geobeagle.DestinationListOnClickListener.DestinationListDialogOnClickListener;

import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class DestinationListOnClickListenerTest extends TestCase {
    OnClickListener mockListener = createMock(OnClickListener.class);

    public void testOnClick() {
        LocationSetter locationSetter = createMock(LocationSetter.class);
        AlertDialog.Builder dialogBuilder = createMock(AlertDialog.Builder.class);
        AlertDialog alertDialog = createMock(AlertDialog.class);

        expect(dialogBuilder.setTitle(R.string.select_destination)).andReturn(dialogBuilder);
        expect(dialogBuilder.setItems(aryEq(new CharSequence[] {
                DestinationListOnClickListener.MY_LOCATION, "OAK", "SFO"
        }), (OnClickListener)eq(mockListener))).andReturn(dialogBuilder);

        expect(dialogBuilder.create()).andReturn(alertDialog);
        alertDialog.show();

        replay(dialogBuilder);
        replay(alertDialog);
        DescriptionsAndLocations descriptionsAndLocations = new DescriptionsAndLocations();
        descriptionsAndLocations.add("SFO", "37 122 etc");
        descriptionsAndLocations.add("OAK", "37 122 foo");

        DestinationListOnClickListener destinationListOnClickListener = new DestinationListOnClickListener(
                descriptionsAndLocations, locationSetter, dialogBuilder, null) {
            protected OnClickListener createDestinationListDialogOnClickListener(
                    List<CharSequence> previousLocations) {
                assertEquals(Arrays.asList(null, "37 122 foo", "37 122 etc"), previousLocations);
                return mockListener;
            }
        };
        destinationListOnClickListener.onClick(null);
        verify(dialogBuilder);
        verify(alertDialog);
    }

    public void testDestinationListDialogOnClickListener() {
        LocationSetter locationSetter = createMock(LocationSetter.class);
        CharSequence[] locations = new CharSequence[] {
                null, "37 122 foo", "37 122 etc",
        };
        locationSetter.setLocation("37 122 foo", null);

        replay(locationSetter);
        DestinationListDialogOnClickListener dldocl = new DestinationListDialogOnClickListener(
                Arrays.asList(locations), locationSetter, null);
        dldocl.onClick(null, 1);
        verify(locationSetter);
    }
}
