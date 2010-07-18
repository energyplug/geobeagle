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

import com.google.code.geobeagle.cachedetails.WriterWrapper;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;

class TagWriter {
    private static final String SPACES = "                        ";
    private int mLevel;
    private WriterWrapper writer;

    @Inject
    public TagWriter() {
        this.writer = new WriterWrapper();
    }

    public void close() throws IOException {
        writer.close();
    }

    public void endTag(String name) throws IOException {
        mLevel--;
        if (writer != null)
            writer.write("</" + name + ">");
    }

    public boolean isOpen() {
        return writer.isOpen();
    }

    public void open(String path) throws IOException {
        mLevel = 0;
        new File(new File(path).getParent()).mkdirs();
        writer.open(path);
        writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
    }

    public void startTag(Tag tag) throws IOException {
        writeNewline();
        mLevel++;
        writer.write("<" + tag.name);
        for (String key : tag.attributes.keySet()) {
            writer.write(" " + key + "='" + tag.attributes.get(key) + "'");
        }
        writer.write(">");
    }

    private void writeNewline() throws IOException {
        writer.write("\n" + SPACES.substring(0, Math.min(mLevel, SPACES.length())));
    }

    public void text(String text) throws IOException {
        writer.write(text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"));
    }
}
