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

package com.google.code.geobeagle.xmlimport;

import com.google.code.geobeagle.ErrorDisplayer;
import com.google.code.geobeagle.R;
import com.google.code.geobeagle.xmlimport.GpxToCache.CancelException;
import com.google.inject.Provider;

import org.xmlpull.v1.XmlPullParserException;

import android.database.sqlite.SQLiteException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

public class GpxLoader {
    private final ErrorDisplayer mErrorDisplayer;
    private final GpxToCache mGpxToCache;
    private final Provider<ImportWakeLock> mImportWakeLockProvider;
    public static final int WAKELOCK_DURATION = 15000;

    public GpxLoader(ErrorDisplayer errorDisplayer,
            GpxToCache gpxToCache,
            Provider<ImportWakeLock> importWakeLockProvider) {
        mGpxToCache = gpxToCache;
        mErrorDisplayer = errorDisplayer;
        mImportWakeLockProvider = importWakeLockProvider;
    }

    public void end() {
        mGpxToCache.end();
    }

    /**
     * @return true if we should continue loading more files, false if we should
     *         terminate.
     */
    public boolean load(String path, Reader reader) {
        boolean markLoadAsComplete = false;
        boolean continueLoading = false;
        try {
            final String filename = new File(path).getName();
            mGpxToCache.open(path, filename, reader);

            mImportWakeLockProvider.get().acquire(WAKELOCK_DURATION);
            boolean alreadyLoaded = mGpxToCache.load();
            markLoadAsComplete = !alreadyLoaded;
            continueLoading = true;
        } catch (SQLiteException e) {
            mErrorDisplayer.displayError(R.string.error_writing_cache, mGpxToCache.getSource()
                    + ": " + e.getMessage());
        } catch (XmlPullParserException e) {
            mErrorDisplayer.displayError(R.string.error_parsing_file, mGpxToCache.getSource()
                    + ": " + e.getMessage());
        } catch (FileNotFoundException e) {
            mErrorDisplayer.displayError(R.string.file_not_found, mGpxToCache.getSource() + ": "
                    + e.getMessage());
        } catch (IOException e) {
            mErrorDisplayer.displayError(R.string.error_reading_file, mGpxToCache.getSource()
                    + ": " + e.getMessage());
        } catch (CancelException e) {
        }

        mGpxToCache.close(markLoadAsComplete);
        return continueLoading;
    }

    public void start() {
        mGpxToCache.start();
    }
}
