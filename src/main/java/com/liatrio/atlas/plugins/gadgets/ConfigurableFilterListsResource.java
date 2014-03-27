package com.liatrio.atlas.plugins.gadgets;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.liatrio.atlas.plugins.structs.FilterPair;
import com.liatrio.atlas.plugins.structs.FilterPairList;

@Path("configurablefilterlists")
@AnonymousAllowed
public class ConfigurableFilterListsResource {
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SearchRequestService searchRequestService;

    public ConfigurableFilterListsResource(
            JiraAuthenticationContext jiraAuthenticationContext,
            SearchRequestService searchRequestService) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.searchRequestService = searchRequestService;
    }

    @GET
    @Path("listfilters")
    @Produces({MediaType.APPLICATION_JSON})
    public Response validatedd() {
        User currentUser = jiraAuthenticationContext.getLoggedInUser();

        final SharedEntitySearchParametersBuilder searchParameters = new SharedEntitySearchParametersBuilder();
        searchParameters.setSortColumn(SharedEntityColumn.ID, true);
        SharedEntitySearchParameters params = searchParameters.toSearchParameters();
        SharedEntitySearchResult<SearchRequest> searchResult = searchRequestService.search(new JiraServiceContextImpl(currentUser), params, 0, Integer.MAX_VALUE);
        Collection<SearchRequest> filters = searchResult.getResults();

        FilterPairList list = new FilterPairList();
        for (SearchRequest sr : filters) {
            list.addFilterPair(new FilterPair(sr.getName(), "filter-" + sr.getId()));
        }

        return Response.ok(list).cacheControl(CacheControl.NO_CACHE).build();
    }
}
