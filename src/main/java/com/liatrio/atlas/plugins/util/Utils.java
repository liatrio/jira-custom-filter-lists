package com.liatrio.atlas.plugins.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utils {
    public static List<Long> getFilterIds(String filterIdsStr) {
        List<Long> ids = new ArrayList<Long>();
        if (!isNormal(filterIdsStr)) {
            return ids;
        }
        return ids;
    }

    public static boolean isNormal(Collection<?> collection) {
        return collection != null && collection.size() > 0;
    }

    public static boolean isNormal(String str) {
        return str != null && str.length() > 0;
    }

    private Utils() {
    }
}
