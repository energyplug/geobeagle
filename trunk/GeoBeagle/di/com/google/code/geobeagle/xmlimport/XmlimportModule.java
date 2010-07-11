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


import com.google.code.geobeagle.cachedetails.CacheDetailsWriter;
import com.google.code.geobeagle.cachedetails.Emotifier;
import com.google.code.geobeagle.cachedetails.HtmlWriter;
import com.google.code.geobeagle.cachedetails.StringWriterWrapper;
import com.google.code.geobeagle.database.GpxWriter;
import com.google.code.geobeagle.xmlimport.GpxImporterDI.MessageHandler;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import roboguice.config.AbstractAndroidModule;
import roboguice.inject.ContextScoped;

import android.content.Context;

public class XmlimportModule extends AbstractAndroidModule {

    @Override
    protected void configure() {
        bind(StringWriterWrapper.class).in(Singleton.class);
        bind(MessageHandler.class).in(ContextScoped.class);
        bind(XmlPullParserWrapper.class).in(ContextScoped.class);
        bind(GpxWriter.class).in(ContextScoped.class);
        bind(EmotifierPatternProvider.class).in(Singleton.class);
    }

    @Provides
    CacheDetailsWriter cacheDetailsWriterLoadDetailsProvider(Emotifier emotifier, Context context,
            StringWriterWrapper stringWriterWrapper) {
        final HtmlWriter htmlWriter = new HtmlWriter(stringWriterWrapper);
        return new CacheDetailsWriter(htmlWriter, emotifier, context);
    }

}
