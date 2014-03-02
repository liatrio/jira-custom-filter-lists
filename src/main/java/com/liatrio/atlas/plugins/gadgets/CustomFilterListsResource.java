package com.liatrio.atlas.plugins.gadgets;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.liatrio.atlas.plugins.structs.CustomFilterListsJson;

@Path("customfilterlists")
@AnonymousAllowed
public class CustomFilterListsResource {
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public CustomFilterListsResource(
            ApplicationProperties applicationProperties,
            JiraAuthenticationContext jiraAuthenticationContext) {
        this.applicationProperties = applicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @GET
    @Path("generate")
    @Produces({MediaType.APPLICATION_JSON})
    public Response generate(
            @QueryParam("customTitle") String customTitle,
            @QueryParam("displayStyle") String displayStyle,
            @QueryParam("includeLinks") String includeLinks,
            @QueryParam("includeCounts") String includeCounts,
            @QueryParam("filterListFilter") String filterListFilter,
            @QueryParam("filterIDs") String filterIDs,
            @QueryParam("onlyFavourites") String onlyFavourites) throws IOException {
        CustomFilterListsJson json = new CustomFilterListsJson();
        json.setCustomTitle(customTitle);

        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return Response.ok(json).cacheControl(cc).build();
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

/**
package com.sourcelabs.jira.plugin.portlet.filterlist;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.opensymphony.user.User;
import com.sourcelabs.jira.plugin.portlet.filterlist.FilterListPortlet.1;
import com.sourcelabs.jira.plugin.portlet.filterlist.FilterListPortlet.NullSharedEntitySearchParameters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

public class FilterListPortlet extends PortletImpl {

   private final PermissionManager permissionManager;
   private final ConstantsManager constantsManager;
   private final SearchProvider searchProvider;
   private final ApplicationProperties applicationProperties;
   private final SearchRequestService searchRequestService;


   public FilterListPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ConstantsManager constantsManager, SearchProvider searchProvider, ApplicationProperties applicationProperties, SearchRequestService searchRequestService) {
      super(authenticationContext);
      this.permissionManager = permissionManager;
      this.constantsManager = constantsManager;
      this.searchProvider = searchProvider;
      this.applicationProperties = applicationProperties;
      this.searchRequestService = searchRequestService;
   }

   protected Map getVelocityParams(PortletConfiguration portletConfiguration) {
      HashMap params = new HashMap();
      params.put("portlet", this);
      params.put("portletId", portletConfiguration.getId());

      try {
         String oce = portletConfiguration.getProperty("filterlist-onlyfavourites");
         String filterRegexp = portletConfiguration.getProperty("filterlist-regexp");
         String filterDisplay = portletConfiguration.getProperty("filterlist-display");
         params.put("displayAsList", new Boolean(!"dropdown".equalsIgnoreCase(filterDisplay)));
         String filterTitle = portletConfiguration.getProperty("filterlist-title");
         params.put("filterTitle", filterTitle);
         String filterIdsConf = portletConfiguration.getTextProperty("filterlist-ids");
         String[] filterIds = filterIdsConf.split("[, \n]");
         params.put("indexing", new Boolean(this.applicationProperties.getOption("jira.option.indexing")));
         boolean loggedIn = this.authenticationContext.getUser() != null;
         params.put("loggedin", new Boolean(loggedIn));
         String includeLinks = portletConfiguration.getProperty("filterlist-includelinks");
         params.put("includeLinks", new Boolean("true".equals(includeLinks)));
         String includeCounts = portletConfiguration.getProperty("filterlist-includecounts");
         params.put("includeCounts", new Boolean("true".equals(includeCounts)));
         if(loggedIn) {
            User user = this.authenticationContext.getUser();
            Object filters = null;
            if("true".equals(oce)) {
               filters = this.searchRequestService.getFavouriteFilters(user);
            } else {
               SharedEntitySearchResult iterator = this.searchRequestService.search(new JiraServiceContextImpl(user), new NullSharedEntitySearchParameters(this, (1)null), 0, 400);
               filters = iterator.getResults();
            }

            if(filterIds != null && filters != null && filterIds.length != 0 && (filterIds.length != 1 || !filterIds[0].equals(""))) {
               HashMap var21 = new HashMap();
               Iterator pse = ((Collection)filters).iterator();

               String filterId;
               while(pse.hasNext()) {
                  SearchRequest i = (SearchRequest)pse.next();
                  filterId = i.getId().toString();
                  var21.put(filterId, i);
               }

               filters = new ArrayList();

               for(int var23 = 0; var23 < filterIds.length; ++var23) {
                  filterId = filterIds[var23];
                  if(var21.containsKey(filterId)) {
                     ((Collection)filters).add(var21.get(filterId));
                  }
               }
            }

            if(filterRegexp != null && !"".equals(filterRegexp)) {
               Iterator var22 = ((Collection)filters).iterator();

               try {
                  while(var22.hasNext()) {
                     SearchRequest var20 = (SearchRequest)var22.next();
                     if(!var20.getName().matches(filterRegexp)) {
                        var22.remove();
                     }
                  }
               } catch (PatternSyntaxException var18) {
                  params.put("regexWarning", var18.getMessage());
               }
            }

            params.put("chosenFilters", filters);
         }
      } catch (ObjectConfigurationException var19) {
         var19.printStackTrace();
      }

      return params;
   }

   public long getCountsForFilter(SearchRequest sr) throws SearchException {
      User user = this.authenticationContext.getUser();
      return this.searchProvider.searchCount(sr.getQuery(), user);
   }
}

**/