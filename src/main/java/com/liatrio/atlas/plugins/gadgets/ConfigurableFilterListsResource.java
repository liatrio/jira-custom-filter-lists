package com.liatrio.atlas.plugins.gadgets;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.liatrio.atlas.plugins.structs.CustomFilterListsJson;
import com.liatrio.atlas.plugins.structs.FilterPair;
import com.liatrio.atlas.plugins.structs.FilterPairList;
import com.liatrio.atlas.plugins.util.Utils;

@Path("configurablefilterlists")
@AnonymousAllowed
public class ConfigurableFilterListsResource {
    private static final String TEMPLATE = "com/liatrio/atlas/plugins/customfilterlists/templates/customfilterlist.vm";

    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SearchProvider searchProvider;
    private final SearchRequestService searchRequestService;
    private final TemplateRenderer renderer;

    public ConfigurableFilterListsResource(
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
            @QueryParam("columnNames") List<String> columnNames,
            @QueryParam("customTitle") String customTitle,
            @QueryParam("displayStyle") String displayStyle,
            @QueryParam("includeLinks") String includeLinks,
            @QueryParam("includeCounts") String includeCounts) throws IOException {
        Map<String, Object> context = new HashMap<String, Object>();

        User currentUser = jiraAuthenticationContext.getLoggedInUser();
        JiraServiceContext jiraServiceCtx = new JiraServiceContextImpl(currentUser);
        boolean loggedIn = currentUser != null;

        List<SearchRequest> filters = new ArrayList<SearchRequest>();
        List<Long> ids = Utils.parseFilters(columnNames);
        for (Long id : ids) {
            SearchRequest sr = searchRequestService.getFilter(jiraServiceCtx, id);
            if (sr != null) {
                filters.add(sr);
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

        return Response.ok(json).cacheControl(CacheControl.NO_CACHE).build();
    }

    @GET
    @Path("listfilters")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllFilters() {
        User currentUser = jiraAuthenticationContext.getLoggedInUser();

        final SharedEntitySearchParametersBuilder searchParameters = new SharedEntitySearchParametersBuilder();
        searchParameters.setSortColumn(SharedEntityColumn.ID, true);
        SharedEntitySearchParameters params = searchParameters.toSearchParameters();
        SharedEntitySearchResult<SearchRequest> searchResult = searchRequestService.search(new JiraServiceContextImpl(currentUser), params, 0, Integer.MAX_VALUE);
        Collection<SearchRequest> filters = searchResult.getResults();

        FilterPairList list = new FilterPairList();
        for (SearchRequest sr : filters) {
            list.addFilterPair(new FilterPair(sr.getName(), Utils.FILTER_PREFIX + sr.getId()));
        }

        return Response.ok(list).cacheControl(CacheControl.NO_CACHE).build();
    }

    @GET
    @Path("config/validate")
    @Produces({MediaType.APPLICATION_JSON})
    public Response validate(
            @QueryParam("columnNames") List<String> columnNames,
            @QueryParam("customTitle") String customTitle,
            @QueryParam("displayStyle") String displayStyle,
            @QueryParam("includeLinks") String includeLinks,
            @QueryParam("includeCounts") String includeCounts) {
        Collection<ValidationError> validationErrors = new ArrayList<ValidationError>();
        List<Long> ids = Utils.parseFilters(columnNames);
        if (ids.isEmpty()) {
            validationErrors.add(new ValidationError("projectOrFilterId", "customfilterlists.configurablefilterlistsgadget.error.filters.empty"));
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(ErrorCollection.Builder.newBuilder(validationErrors).build()).cacheControl(CacheControl.NO_CACHE).build();
        }
        return Response.ok().cacheControl(CacheControl.NO_CACHE).build();
    }
}
