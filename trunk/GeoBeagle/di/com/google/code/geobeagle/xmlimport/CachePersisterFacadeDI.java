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

import com.google.code.geobeagle.CacheTypeFactory;
import com.google.code.geobeagle.cachedetails.CacheDetailsWriter;
import com.google.code.geobeagle.cachedetails.FilePathStrategy;
import com.google.code.geobeagle.cachedetails.HtmlWriter;
import com.google.code.geobeagle.cachedetails.WriterWrapper;
import com.google.code.geobeagle.database.CacheWriter;
import com.google.code.geobeagle.database.TagWriterImpl;
import com.google.code.geobeagle.database.TagWriterNull;
import com.google.inject.Inject;

import android.os.PowerManager.WakeLock;

import java.io.File;

public class CachePersisterFacadeDI {

    // TODO: Remove class CachePersisterFacadeFactory
    public static class CachePersisterFacadeFactory {
        private final CacheDetailsWriter mCacheDetailsWriter;
        private final CacheTypeFactory mCacheTypeFactory;
        private final FileFactory mFileFactory;
        private final HtmlWriter mHtmlWriter;
        private final MessageHandlerInterface mMessageHandler;
        private final WriterWrapper mWriterWrapper;
        private final TagWriterImpl mTagWriterImpl;
        private final TagWriterNull mTagWriterNull;

        @Inject
        public CachePersisterFacadeFactory(MessageHandlerInterface messageHandler,
                CacheTypeFactory cacheTypeFactory, TagWriterImpl tagWriterImpl,
                TagWriterNull tagWriterNull, FilePathStrategy filePathStrategy) {
            mMessageHandler = messageHandler;
            mFileFactory = new FileFactory();
            mWriterWrapper = new WriterWrapper();
            mHtmlWriter = new HtmlWriter(mWriterWrapper);
            mCacheDetailsWriter = new CacheDetailsWriter(mHtmlWriter, filePathStrategy);
            mCacheTypeFactory = cacheTypeFactory;
            mTagWriterImpl = tagWriterImpl;
            mTagWriterNull = tagWriterNull;
        }

        public CachePersisterFacade create(CacheWriter cacheWriter, WakeLock wakeLock) {
            final CacheTagSqlWriter cacheTagSqlWriter = new CacheTagSqlWriter(cacheWriter,
                    mCacheTypeFactory, mTagWriterImpl, mTagWriterNull);
            return new CachePersisterFacade(cacheTagSqlWriter, mFileFactory, mCacheDetailsWriter,
                    mMessageHandler, wakeLock);
        }
    }

    public static class FileFactory {
        public File createFile(String path) {
            return new File(path);
        }
    }
}
