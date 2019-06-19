package org.endeavourhealth.informationmanager.api.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.informationmanager.common.dal.InformationManagerDAL;
import org.endeavourhealth.informationmanager.common.dal.InformationManagerJDBCDAL;
import org.endeavourhealth.informationmanager.common.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("concepts")
@Metrics(registry = "ConceptsMetricRegistry")
@Api(tags = {"Concepts"})
public class ConceptsEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ConceptsEndpoint.class);

    @GET
    @Path("/mru")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "InformationModel.ConceptsEndpoint.MRU.GET")
    @ApiOperation(value = "List most recently accessed concepts", response = Concept.class)
    public Response getMRU(@Context SecurityContext sc) throws Exception {
        LOG.debug("getMRU");

        SearchResult result = new InformationManagerJDBCDAL().getMRU();

        return Response
            .ok()
            .entity(result)
            .build();
    }

    @GET
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "InformationModel.ConceptsEndpoint.Search.GET")
    @ApiOperation(value = "Search concepts for matching term", response = Concept.class)
    public Response search(@Context SecurityContext sc,
                           @QueryParam("term") @Encoded String term,
                           @QueryParam("size") Integer size,
                           @QueryParam("page") Integer page) throws Exception {
        LOG.debug("search");

        SearchResult result = new InformationManagerJDBCDAL().search(term, size, page, null, null);

        return Response
            .ok()
            .entity(result)
            .build();
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "InformationModel.ConceptsEndpoint.{id}.GET")
    @ApiOperation(value = "Gets a concept", response = Concept.class)
    public Response getConcept(@Context SecurityContext sc,
                           @PathParam("id") String id) throws Exception {
        LOG.debug("getConcept");

        Concept result = new InformationManagerJDBCDAL().getConcept(id);

        return Response
            .ok()
            .entity(result)
            .build();
    }

    @GET
    @Path("/{id}/name")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Timed(absolute = true, name = "InformationModel.ConceptsEndpoint.{id}.name.GET")
    @ApiOperation(value = "Gets a concept name", response = Concept.class)
    public Response getConceptName(@Context SecurityContext sc,
                               @PathParam("id") String id) throws Exception {
        LOG.debug("getConceptName");

        String result = new InformationManagerJDBCDAL().getConceptName(id);

        return Response
            .ok()
            .entity(result)
            .build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "InformationModel.ConceptsEndpoint.Concept.{id}.POST")
    @ApiOperation(value = "Updates a concept", response = Concept.class)
    public Response updateConcept(@Context SecurityContext sc,
                                   String body) throws Exception {
        LOG.debug("updateConcept");

        Concept concept = ObjectMapperPool.getInstance().readValue(body, Concept.class);

        concept = new InformationManagerJDBCDAL().updateConcept(concept);

        return Response
            .ok()
            .entity(concept)
            .build();
    }
}
