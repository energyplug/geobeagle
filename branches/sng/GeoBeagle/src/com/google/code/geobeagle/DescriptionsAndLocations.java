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

import java.util.ArrayList;
import java.util.List;

public class DescriptionsAndLocations {
    private List<CharSequence> mPreviousDescriptions;
    private ArrayList<CharSequence> mPreviousLocations;

    public DescriptionsAndLocations() {
        create(100);
    }

    private void create(int maxSize) {
        mPreviousDescriptions = new ArrayList<CharSequence>();
        mPreviousLocations = new ArrayList<CharSequence>();
    }

    public void add(CharSequence description, CharSequence location) {
        final int ix = mPreviousDescriptions.indexOf(description);
        if (ix >= 0) {
            remove(ix);
        }
        mPreviousDescriptions.add(description);
        mPreviousLocations.add(location);
    }

    public void clear() {
        mPreviousDescriptions.clear();
        mPreviousLocations.clear();
    }

    public List<CharSequence> getPreviousDescriptions() {
        return mPreviousDescriptions;
    }

    public ArrayList<CharSequence> getPreviousLocations() {
        return mPreviousLocations;
    }

    private void remove(int i) {
        mPreviousLocations.remove(i);
        mPreviousDescriptions.remove(i);
    }
}
