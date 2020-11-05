package io.drogue.iot.vorto;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.vorto.mapping.engine.MappingEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/convert")
public class ConvertResource {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertResource.class);

    @Inject
    VortoRepository repository;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @POST
    @Path("/{modelId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response convert(@PathParam("modelId") String modelId, final Map<String, Object> input) {

        var spec = repository.getById(modelId);
        if (spec.isEmpty()) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Model not found")
                    .build();
        }

        LOG.info("Spec: {}", spec.get());

        final MappingEngine engine = MappingEngine.create(spec.get());

        var output = engine
                .mapSource(input);

        return Response.ok(output).build();
    }
}