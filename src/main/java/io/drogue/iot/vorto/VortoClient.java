package io.drogue.iot.vorto;

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * A client to the APIs of Eclipse Vorto that we need.
 *
 * <h3>Authorization</h3>
 * <p>
 * You can provide a token to access the Vorto API through the configuration option {@code apiKey.vorto}. This must be
 * the value of the bearer token, without the {@code Bearer} prefix.
 * </p>
 * <p>
 * If you do not provide the API token, then you will only have access to the public models of the repository.
 * </p>
 */
@Path("/rest")
@RegisterRestClient
public interface VortoClient {

    class ExistsResult {
        public boolean exists;
    }

    @GET
    @Path("/mappings/specifications/{modelId}/exists")
    @ClientHeaderParam(name = "Authorization", value = "{getAuthorizationHeader}", required = false)
    @Produces(MediaType.APPLICATION_JSON)
    ExistsResult mappingExists(@PathParam("modelId") String modelId);


    @GET
    @Path("/mappings/specifications/{modelId}")
    @ClientHeaderParam(name = "Authorization", value = "{getAuthorizationHeader}", required = false)
    @Produces(MediaType.APPLICATION_JSON)
    String getMapping(@PathParam("modelId") String modelId);

    default String[] getAuthorizationHeader() {
        final Config config = ConfigProvider.getConfig();
        final Optional<String> token = config.getOptionalValue("apiKey.vorto", String.class);
        return token
                .map(t ->
                        new String[]{"Bearer " + t}
                )
                .orElseGet(() -> new String[0]);
    }
}
