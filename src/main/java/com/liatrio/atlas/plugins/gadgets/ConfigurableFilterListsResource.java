package com.liatrio.atlas.plugins.gadgets;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.liatrio.atlas.plugins.structs.FilterPair;
import com.liatrio.atlas.plugins.structs.FilterPairList;

@Path("configurablefilterlists")
@AnonymousAllowed
public class ConfigurableFilterListsResource {
    @GET
    @Path("config/validate")
    @Produces({MediaType.APPLICATION_JSON})
    public Response validatedd() {
        FilterPairList list = new FilterPairList();
        list.addFilterPair(new FilterPair("created", "created"));

        return Response.ok(list).cacheControl(CacheControl.NO_CACHE).build();
    }

/**
 * 
 * {"availableColumns":
 *     [{"label":"Affects Version/s","value":"versions"},
 *      {"label":"Assignee","value":"assignee"},{"label":"Component/s","value":"components"},{"label":"Created","value":"created"},{"label":"Description","value":"description"},{"label":"Due Date","value":"duedate"},{"label":"Environment","value":"environment"},{"label":"Fix Version/s","value":"fixVersions"},{"label":"Images","value":"thumbnail"},{"label":"Issue Type","value":"issuetype"},{"label":"Key","value":"issuekey"},{"label":"Labels","value":"labels"},{"label":"Last Viewed","value":"lastViewed"},{"label":"Linked Issues","value":"issuelinks"},{"label":"Original Estimate","value":"timeoriginalestimate"},{"label":"Priority","value":"priority"},{"label":"Progress","value":"progress"},{"label":"Project","value":"project"},{"label":"Remaining Estimate","value":"timeestimate"},{"label":"Reporter","value":"reporter"},{"label":"Resolution","value":"resolution"},{"label":"Resolved","value":"resolutiondate"},{"label":"Security Level","value":"security"},{"label":"Status","value":"status"},{"label":"Sub-Tasks","value":"subtasks"},{"label":"Summary","value":"summary"},{"label":"Time Spent","value":"timespent"},{"label":"Updated","value":"updated"},{"label":"Votes","value":"votes"},{"label":"Watchers","value":"watches"},{"label":"Work Ratio","value":"workratio"},{"label":"Σ Original Estimate","value":"aggregatetimeoriginalestimate"},{"label":"Σ Progress","value":"aggregateprogress"},{"label":"Σ Remaining Estimate","value":"aggregatetimeestimate"},{"label":"Σ Time Spent","value":"aggregatetimespent"}],"defaultColumns":[{"label":"Issue Type","value":"issuetype"},{"label":"Key","value":"issuekey"},{"label":"Summary","value":"summary"},{"label":"Priority","value":"priority"}]}
 * 
 */
}
