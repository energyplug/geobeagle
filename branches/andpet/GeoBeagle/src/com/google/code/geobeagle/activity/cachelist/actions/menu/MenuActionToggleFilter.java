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

package com.google.code.geobeagle.activity.cachelist.actions.menu;

import com.google.code.geobeagle.R;
import com.google.code.geobeagle.actions.MenuActionBase;
import com.google.code.geobeagle.activity.cachelist.presenter.CacheListRefresh;
import com.google.code.geobeagle.database.CachesProviderToggler;

public class MenuActionToggleFilter extends MenuActionBase {
    private final CachesProviderToggler mCachesProviderToggler;
    private final CacheListRefresh mMenuActionRefresh;

    public MenuActionToggleFilter(CachesProviderToggler cachesProviderToggler,
            CacheListRefresh cacheListRefresh) {
        super(R.string.menu_toggle_filter);
        mCachesProviderToggler = cachesProviderToggler;
        mMenuActionRefresh = cacheListRefresh;
    }

    public void act() {
        mCachesProviderToggler.toggle();
        mMenuActionRefresh.forceRefresh();
    }
}
