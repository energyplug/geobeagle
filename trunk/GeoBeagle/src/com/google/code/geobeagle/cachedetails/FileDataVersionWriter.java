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

package com.google.code.geobeagle.cachedetails;

import com.google.code.geobeagle.xmlimport.XmlimportAnnotations.DetailsDirectory;
import com.google.code.geobeagle.xmlimport.XmlimportAnnotations.VersionPath;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;

public class FileDataVersionWriter {
    private final WriterWrapper writerWrapper;
    private final String versionDirectory;
    private final String versionPath;

    @Inject
    FileDataVersionWriter(WriterWrapper writerWrapper, @DetailsDirectory String detailsDirectory,
            @VersionPath String versionPath) {
        this.writerWrapper = writerWrapper;
        this.versionDirectory = detailsDirectory;
        this.versionPath = versionPath;
    }

    public void writeVersion() throws IOException {
        new File(versionDirectory).mkdir();
        writerWrapper.open(versionPath);
        writerWrapper.write("0");
        writerWrapper.close();
    }
}
