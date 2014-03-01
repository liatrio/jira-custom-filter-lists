package com.liatrio.atlas.plugins.gadgets;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.rest.v1.util.CacheControl;
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
    public Response generate() throws IOException {
        CustomFilterListsJson json = new CustomFilterListsJson();
        return Response.ok(json).cacheControl(CacheControl.NO_CACHE).build();
    }

    @GET
    @Path("config/validate")
    @Produces({MediaType.APPLICATION_JSON})
    public Response validate() {
        return Response.ok().cacheControl(CacheControl.NO_CACHE).build();
        //return Response.status(HttpStatus.SC_BAD_REQUEST).entity(ErrorCollection.Builder.newBuilder(validationErrors).build()).cacheControl(CacheControl.NO_CACHE).build();
    }
}
