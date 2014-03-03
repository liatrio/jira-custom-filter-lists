package com.liatrio.atlas.plugins.gadgets;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.opensymphony.util.TextUtils;

public class GadgetRenderer {
    private final User user;
    private final SearchProvider searchProvider;

    public GadgetRenderer(User user, SearchProvider searchProvider) {
        this.user = user;
        this.searchProvider = searchProvider;
    }

    public String encodeText(String text) {
        return TextUtils.htmlEncode(text);
    }

    public long getCountsForFilter(SearchRequest sr) throws SearchException {
        return searchProvider.searchCount(sr.getQuery(), user);
    }
}
