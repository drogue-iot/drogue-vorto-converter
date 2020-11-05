package io.drogue.iot.vorto;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.vorto.mapping.engine.MappingEngine;
import org.eclipse.vorto.mapping.targetplatform.ditto.TwinPayloadFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;

@Path("/ce")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Converter {

    @Inject
    VortoRepository repository;

    @POST
    public Response convert(final CloudEvent event) {

        var modelId = event.getAttribute("model_id");
        var deviceId = event.getAttribute("device_id");

        if (!(modelId instanceof String)) {
            Response.ok(event);
        }

        if (!(deviceId instanceof String)) {
            Response.ok(event);
        }

        var data = event.getData();

        var spec = repository.getById(modelId.toString());
        if (spec.isEmpty()) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Model not found")
                    .build();
        }

        final MappingEngine engine = MappingEngine.create(spec.get());

        var output = engine
                .mapSource(data);

        var ditto = TwinPayloadFactory.toDittoProtocol(output, deviceId.toString());
        Gson gson = new GsonBuilder().create();

        var newData = gson.toJson(ditto);

        var result = new CloudEventBuilder(event)
                .withData("text/json", newData.getBytes(StandardCharsets.UTF_8))
                .build();

        return Response.ok(result).build();
    }

}
