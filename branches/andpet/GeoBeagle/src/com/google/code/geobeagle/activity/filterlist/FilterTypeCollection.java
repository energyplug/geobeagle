package com.google.code.geobeagle.activity.filterlist;

import com.google.code.geobeagle.CacheFilter;
import com.google.code.geobeagle.Labels;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FilterTypeCollection {
    private static final String FILTER_PREFS = "Filter";
    private final Activity mActivity;
    private ArrayList<CacheFilter> mFilterTypes = new ArrayList<CacheFilter>();
    
    public FilterTypeCollection(Activity activity) {
        mActivity = activity;
        load();
    }
    
    private void load() {
        SharedPreferences prefs = mActivity.getSharedPreferences(FILTER_PREFS, 0);
        String ids = prefs.getString("FilterList", "");
        String[] idArray = ids.split(", ");
        if (idArray.length == 1 && idArray[0].equals("")) {
            firstSetup();
        } else {
            for (String id : idArray) {
                mFilterTypes.add(new CacheFilter(id, mActivity));
            }
        }
    }

    private void firstSetup() {
        Log.d("GeoBeagle", "FilterTypeCollection first setup");
        add(new CacheFilter("All", mActivity, new FilterPreferences("All caches")));

        {   FilterPreferences favoritesPref = new FilterPreferences("Favorites");
            favoritesPref.setInteger("FilterLabel", Labels.FAVORITES);
            add(new CacheFilter("Favorites", mActivity, favoritesPref));
        }

        {   FilterPreferences foundPref = new FilterPreferences("Found");
            foundPref.setInteger("FilterLabel", Labels.FOUND);
            add(new CacheFilter("Found", mActivity, foundPref));
        }

        {   FilterPreferences dnfPref = new FilterPreferences("Did Not Find");
            dnfPref.setInteger("FilterLabel", Labels.DNF);
            add(new CacheFilter("DNF", mActivity, dnfPref));
        }
        
        String filterList = null;
        for (CacheFilter cacheFilter : mFilterTypes) {
            cacheFilter.saveToPreferences();
            if (filterList == null)
                filterList = cacheFilter.mId;
            else
                filterList = filterList + ", " + cacheFilter.mId;
        }
        SharedPreferences prefs = mActivity.getSharedPreferences(FILTER_PREFS, 0);
        Editor editor = prefs.edit();
        editor.putString("FilterList", filterList);
        editor.commit();
    }
    
    private void add(CacheFilter cacheFilter) {
        mFilterTypes.add(cacheFilter);
    }
    
    private CacheFilter getFromId(String id) {
        for (CacheFilter cacheFilter : mFilterTypes)
            if (cacheFilter.mId.equals(id))
                return cacheFilter;
        return null;
    }
    
    public CacheFilter getActiveFilter() {
        SharedPreferences prefs = mActivity.getSharedPreferences(FILTER_PREFS, 0);
        String id = prefs.getString("ActiveFilter", "All");
        return getFromId(id);
    }
    
    public void setActiveFilter(CacheFilter cacheFilter) {
        SharedPreferences prefs = mActivity.getSharedPreferences(FILTER_PREFS, 0);
        Editor editor = prefs.edit();
        editor.putString("ActiveFilter", cacheFilter.mId);
    }
    
    public ArrayList<Map<String, String>> getAdapterData() {
        ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (CacheFilter cacheFilter : mFilterTypes) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("label", cacheFilter.getName());
            data.add(item);
        }
        return data;
    }

    public CacheFilter get(int position) {
        return mFilterTypes.get(position);
    }
    
    public int getIndexOf(CacheFilter cacheFilter) {
        return mFilterTypes.indexOf(cacheFilter);
    }
}
