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

package com.google.code.geobeagle.cacheloader;

import com.google.code.geobeagle.cachedetails.FilePathStrategy;
import com.google.code.geobeagle.cachedetails.reader.DetailsReader;
import com.google.code.geobeagle.xmlimport.CacheTagsToDetails;
import com.google.inject.Inject;

import java.io.File;

public class CacheDetailsLoader {

    private final CacheTagsToDetails mCacheTagsToDetails;
    private final DetailsOpener mDetailsOpener;
    private final FilePathStrategy mFilePathStrategy;

    @Inject
    CacheDetailsLoader(DetailsOpener detailsOpener,
            FilePathStrategy filePathStrategy,
            CacheTagsToDetails cacheTagsToDetails) {
        mDetailsOpener = detailsOpener;
        mFilePathStrategy = filePathStrategy;
        mCacheTagsToDetails = cacheTagsToDetails;
    }

    public String load(CharSequence sourceName, CharSequence cacheId) throws CacheLoaderException {
        String path = mFilePathStrategy.getPath(sourceName, cacheId.toString(), "gpx");
        File file = new File(path);
        DetailsReader detailsReader = mDetailsOpener.open(file);
        return detailsReader.read(mCacheTagsToDetails);
    }
}