package io.drogue.iot.vorto;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
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
    private static final Gson GSON = new GsonBuilder().create();

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

        LOG.debug("Converting - modelId: {}, deviceId: {}", modelId, deviceId);

        if (!(modelId instanceof String)) {
            return Response.ok(event).build();
        }

        if (!(deviceId instanceof String)) {
            return Response.ok(event).build();
        }

        LOG.debug("CloudEvent: {}", event);

        final Object data;
        if (isJson(event.getDataContentType())) {
            data = GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(event.getData()), StandardCharsets.UTF_8), Map.class);
        } else {
            data = event.getData();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Data: {}", data);
        }

        var spec = repository.getById(modelId.toString());
        if (spec.isEmpty()) {
            LOG.debug("Model {} not found", modelId);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorInformation("ModelNotFound", String.format("Unable to find model: '%s'", modelId)))
                    .build();
        }

        var mapping = spec.get();

        LOG.debug("Mapping specification: {}", mapping);

        final MappingEngine engine = MappingEngine.create(mapping);

        var output = engine
                .mapSource(data);

        var ditto = TwinPayloadFactory.toDittoProtocol(output, deviceId.toString());

        var newData = GSON.toJson(ditto);

        var result = new CloudEventBuilder(event)
                .withData(MediaType.APPLICATION_JSON, newData.getBytes(StandardCharsets.UTF_8))
                .withDataSchema(URI.create("ditto:" + modelId))
                .build();

        LOG.debug("Outcome: {}", newData);

        return Response.ok(result).build();
    }

    /**
     * Test if a mime type reflects JSON.
     *
     * @param type The type to test.
     * @return {@code true} if the type is JSON, {@code false} otherwise.
     */
    static boolean isJson(final String type) {

        if (type == null) {
            return false;
        }

        final MimeType parsed;
        try {
            parsed = new MimeType(type);
        } catch (MimeTypeParseException e) {
            return false;
        }

        var sub = parsed.getSubType();

        if (sub.equals("json")) {
            return true;
        }

        if (sub.endsWith("+json")) {
            return true;
        }

        return false;

    }

}
