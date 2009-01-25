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

package com.google.code.geobeagle.ui;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import com.google.code.geobeagle.R;
import com.google.code.geobeagle.ResourceProvider;

import junit.framework.TestCase;

public class OnContentProviderSelectedListenerTest extends TestCase {

    public void testOnContentProviderSelectedListener() {
        MockableTextView contentProviderCaption = createMock(MockableTextView.class);
        MockableTextView gotoObjectCaption = createMock(MockableTextView.class);
        ResourceProvider resourceProvider = createMock(ResourceProvider.class);
        expect(resourceProvider.getStringArray(R.array.object_names)).andReturn(new String[] {
                "letterbox", "geocache"
        });
        contentProviderCaption.setText("Search for letterbox:");
        contentProviderCaption.setText("Search for geocache:");
        gotoObjectCaption.setText("Go to letterbox:");
        gotoObjectCaption.setText("Go to geocache:");

        replay(resourceProvider);
        replay(contentProviderCaption);
        replay(gotoObjectCaption);
        OnContentProviderSelectedListener ocpsl = new OnContentProviderSelectedListener(
                resourceProvider, contentProviderCaption, gotoObjectCaption);
        ocpsl.onItemSelected(null, null, 0, 0);
        ocpsl.onItemSelected(null, null, 1, 0);
        verify(resourceProvider);
        verify(contentProviderCaption);
        verify(gotoObjectCaption);
    }
}
