package com.liatrio.atlas.plugins.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class Utils {
    public static List<Long> getFilterIds(String filterIdsStr) {
        List<Long> ids = new ArrayList<Long>();
        if (!isNormal(filterIdsStr)) {
            return ids;
        }
        StringTokenizer st = new StringTokenizer(filterIdsStr);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            ids.add(Long.valueOf(token));
        }
        return ids;
    }

    public static boolean isNormal(Collection<?> collection) {
        return collection != null && collection.size() > 0;
    }

    public static boolean isNormal(String str) {
        return str != null && str.trim().length() > 0;
    }

    private Utils() {
    }
}
