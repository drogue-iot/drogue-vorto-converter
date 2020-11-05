package io.drogue.iot.vorto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/rest")
@RegisterRestClient
public interface VortoClient {

    class ExistsResult {
        public boolean exists;
    }

    @GET
    @Path("/mappings/specifications/{modelId}/exists")
    @ClientHeaderParam(name = "Authorization", value = "{getAuthorizationHeader}")
    @Produces(MediaType.APPLICATION_JSON)
    ExistsResult exists(@PathParam("modelId") String modelId);


    @GET
    @Path("/mappings/specifications/{modelId}")
    @ClientHeaderParam(name = "Authorization", value = "{getAuthorizationHeader}")
    @Produces(MediaType.APPLICATION_JSON)
    String getModel(@PathParam("modelId") String modelId);

    default String getAuthorizationHeader() {
        final Config config = ConfigProvider.getConfig();
        final String token = config.getValue("apiKey.vorto", String.class);
        return "Bearer " + token;
    }
}
