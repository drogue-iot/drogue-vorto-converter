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
import org.eclipse.vorto.mapping.targetplatform.ditto.TwinPayloadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    @Path("/{modelId}/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response convert(@PathParam("modelId") final String modelId, @PathParam("deviceId") final String deviceId, final Map<String, Object> input) {

        var spec = repository.getById(modelId);
        if (spec.isEmpty()) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Model not found")
                    .build();
        }

        final MappingEngine engine = MappingEngine.create(spec.get());

        var output = engine
                .mapSource(input);

        var ditto = TwinPayloadFactory.toDittoProtocol(output, deviceId);
        Gson gson = new GsonBuilder().create();

        return Response.ok(gson.toJson(ditto)).build();
    }

}