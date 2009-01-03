
package com.google.code.geobeagle.intents;

import com.google.code.geobeagle.UriParser;

import android.content.Intent;

public class IntentFactory {
    private final UriParser mUriParser;

    public IntentFactory(UriParser uriParser) {
        mUriParser = uriParser;
    }

    public Intent createIntent(String action) {
        return new Intent(action);
    }

    public Intent createIntent(String action, String uri) {
        return new Intent(action, mUriParser.parse(uri));
    }

}