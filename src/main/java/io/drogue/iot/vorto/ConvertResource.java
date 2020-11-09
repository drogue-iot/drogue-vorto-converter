package io.drogue.iot.vorto;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
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

import io.quarkus.arc.profile.IfBuildProfile;

@Path("/convert")
@IfBuildProfile("dev")
public class ConvertResource {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertResource.class);

    @Inject
    VortoRepository repository;

    @POST
    @Path("/{modelId}/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response convert(@PathParam("modelId") final String modelId, @PathParam("deviceId") final String deviceId, final Map<String, Object> input) {

        var spec = repository.getById(modelId);
        if (spec.isEmpty()) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorInformation("ModelNotFound", String.format("Unable to find model: '%s'", modelId)))
                    .build();
        }

        final MappingEngine engine = MappingEngine.create(spec.get());

        var output = engine
                .mapSource(input);

        var ditto = TwinPayloadFactory.toDittoProtocol(output, deviceId);
        Gson gson = new GsonBuilder().create();

        LOG.info("Schema UIR: {}", URI.create("ditto:" + modelId));

        return Response.ok(gson.toJson(ditto)).build();
    }

}