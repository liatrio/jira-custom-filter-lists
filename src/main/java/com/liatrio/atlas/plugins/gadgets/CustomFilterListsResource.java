package com.liatrio.atlas.plugins.gadgets;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.liatrio.atlas.plugins.structs.CustomFilterListsJson;
import com.liatrio.atlas.plugins.util.Utils;

@Path("customfilterlists")
@AnonymousAllowed
public class CustomFilterListsResource {
    private static final String TEMPLATE = "com/liatrio/atlas/plugins/customfilterlists/templates/customfilterlist.vm";

    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SearchProvider searchProvider;
    private final SearchRequestService searchRequestService;
    private final TemplateRenderer renderer;

    public CustomFilterListsResource(
            ApplicationProperties applicationProperties,
            JiraAuthenticationContext jiraAuthenticationContext,
            SearchProvider searchProvider,
            SearchRequestService searchRequestService,
            TemplateRenderer renderer) {
        this.applicationProperties = applicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.searchProvider = searchProvider;
        this.searchRequestService = searchRequestService;
        this.renderer = renderer;
    }

    @GET
    @Path("generate")
    @Produces({MediaType.APPLICATION_JSON})
    public Response generate(
            @Context HttpServletRequest request,
            @QueryParam("customTitle") String customTitle,
            @QueryParam("displayStyle") String displayStyle,
            @QueryParam("includeLinks") String includeLinks,
            @QueryParam("includeCounts") String includeCounts,
            @QueryParam("filterListFilter") String filterListFilter,
            @QueryParam("filterIDs") String filterIDs,
            @QueryParam("onlyFavourites") String onlyFavourites) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();

        User currentUser = jiraAuthenticationContext.getLoggedInUser();
        boolean loggedIn = currentUser != null;

        Collection<SearchRequest> filters = null;
        if(Boolean.valueOf(onlyFavourites)) {
            filters = searchRequestService.getFavouriteFilters(currentUser);
        } else {
            final SharedEntitySearchParametersBuilder searchParameters = new SharedEntitySearchParametersBuilder();
            searchParameters.setSortColumn(SharedEntityColumn.FAVOURITE_COUNT, false);
            SharedEntitySearchParameters params = searchParameters.toSearchParameters();
            SharedEntitySearchResult<SearchRequest> searchResult = searchRequestService.search(new JiraServiceContextImpl(currentUser), params, 0, 400);
            filters = searchResult.getResults();
        }

        List<Long> ids = Utils.getFilterIds(filterIDs);
        if(Utils.isNormal(ids) && Utils.isNormal(filters)) {
            Map<Long, SearchRequest> filterMap = new HashMap<Long, SearchRequest>();
            for (SearchRequest filter : filters) {
                filterMap.put(filter.getId(), filter);
            }
            filters = new ArrayList<SearchRequest>();
            for (Long id : ids) {
                if (filterMap.containsKey(id)) {
                    filters.add(filterMap.get(id));
                }
            }
        }

        if(Utils.isNormal(filterListFilter)) {
            Iterator<SearchRequest> filterIter = filters.iterator();
            while (filterIter.hasNext()) {
                SearchRequest filter = filterIter.next();
                if (!filter.getName().matches(filterListFilter)) {
                    filterIter.remove();
                }
            }
        }

        context.put("loggedin", new Boolean(loggedIn));
        context.put("ctxPath", request.getContextPath());
        context.put("includeLinks", Boolean.valueOf(includeLinks));
        context.put("includeCounts", Boolean.valueOf(includeCounts));
        context.put("indexing", new Boolean(applicationProperties.getOption("jira.option.indexing")));
        context.put("displayAsList", new Boolean(!"dropdown".equalsIgnoreCase(displayStyle)));
        context.put("chosenFilters", filters);
        context.put("renderer", new GadgetRenderer(currentUser, searchProvider));

        StringWriter writer = new StringWriter();
        renderer.render(TEMPLATE, context, writer);

        CustomFilterListsJson json = new CustomFilterListsJson();
        json.setCustomTitle(customTitle);
        json.setHtml(writer.toString());

        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return Response.ok(json).cacheControl(cc).build();
    }

    public long getCountsForFilter(SearchRequest sr) throws SearchException {
        User user = jiraAuthenticationContext.getLoggedInUser();
        return this.searchProvider.searchCount(sr.getQuery(), user);
    }

    @GET
    @Path("config/validate")
    @Produces({MediaType.APPLICATION_JSON})
    public Response validate(
            @QueryParam("customTitle") String customTitle,
            @QueryParam("displayStyle") String displayStyle,
            @QueryParam("includeLinks") String includeLinks,
            @QueryParam("includeCounts") String includeCounts,
            @QueryParam("filterListFilter") String filterListFilter,
            @QueryParam("filterIDs") String filterIDs,
            @QueryParam("onlyFavourites") String onlyFavourites) {

        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return Response.ok().cacheControl(cc).build();
        //return Response.status(HttpStatus.SC_BAD_REQUEST).entity(ErrorCollection.Builder.newBuilder(validationErrors).build()).cacheControl(CacheControl.NO_CACHE).build();
    }
}
