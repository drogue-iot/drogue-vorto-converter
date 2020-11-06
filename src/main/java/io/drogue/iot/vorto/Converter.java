package io.drogue.iot.vorto;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.vorto.mapping.engine.MappingEngine;
import org.eclipse.vorto.mapping.targetplatform.ditto.TwinPayloadFactory;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;

@Path("/")
public class Converter {

    private static final Logger LOG = LoggerFactory.getLogger(Converter.class);

    @Inject
    VortoRepository repository;

    @POST
    @Path("/debug")
    public Response debug(@Context HttpRequest request, String payload) {
        LOG.info("Caught: {}", request);
        LOG.info("Headers:");

        for (var entry : request.getHttpHeaders().getRequestHeaders().entrySet()) {
            LOG.info("  {} = {}", entry.getKey(), entry.getValue());
        }

        LOG.info("Payload:\n{}", payload);

        return Response.accepted().build();
    }

    @POST
    public Response convert(final CloudEvent event) {

        if (event == null || event.getData() == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorInformation("NoData", "No data in cloud event"))
                    .build();
        }

        var modelId = event.getExtension("model_id");
        var deviceId = event.getExtension("device_id");

        LOG.info("Converting - modelId: {}, deviceId: {}", modelId, deviceId);

        if (!(modelId instanceof String)) {
            return Response.ok(event).build();
        }

        if (!(deviceId instanceof String)) {
            return Response.ok(event).build();
        }

        LOG.info("CloudEvent: {}", event);

        var data = event.getData();

        var spec = repository.getById(modelId.toString());
        if (spec.isEmpty()) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorInformation("ModelNotFound", String.format("Unable to find model: '%s'", modelId)))
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

        LOG.info("Outcome: {}", result);

        return Response.ok(result).build();
    }

}
