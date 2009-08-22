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
import com.google.code.geobeagle.activity.MenuAction;

import java.util.HashMap;

public class MenuActions {
    private final HashMap<Integer, MenuAction> mMenuActions;

    public MenuActions(MenuActionMyLocation menuActionMyLocation,
            MenuActionSearchOnline menuActionSearchOnline,
            MenuActionSyncGpx menuActionSyncGpx,
            MenuActionToggleFilter menuActionToggleFilter) {
        mMenuActions = new HashMap<Integer, MenuAction>();
        mMenuActions.put(R.id.menu_sync, menuActionSyncGpx);
        mMenuActions.put(R.id.menu_toggle_filter, menuActionToggleFilter);
        mMenuActions.put(R.id.menu_my_location, menuActionMyLocation);
        mMenuActions.put(R.id.menu_search_online, menuActionSearchOnline);
    }

    public void act(int id) {
        mMenuActions.get(id).act();
    }
}
