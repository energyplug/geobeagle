
package com.google.code.geobeagle;

import java.util.List;

public interface LocationSetter {
    public abstract CharSequence setLocation(double lat, double lon, CharSequence description);

    public abstract CharSequence getLocation();

    public abstract void setLocation(CharSequence c, ErrorDisplayer errorDisplayer);

    public abstract List<CharSequence> getPreviousDescriptions();

    public abstract List<CharSequence> getPreviousLocations();

    public abstract DescriptionsAndLocations getDescriptionsAndLocations();

    public abstract void load();

    public abstract void save();
}
