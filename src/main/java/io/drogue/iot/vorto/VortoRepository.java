package io.drogue.iot.vorto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.vorto.mapping.engine.model.spec.IMappingSpecification;

import io.quarkus.cache.CacheResult;

@Singleton
public class VortoRepository {

    @Inject
    @RestClient
    VortoClient client;

    @CacheResult(cacheName = "mapping-cache")
    public Optional<IMappingSpecification> getById(String id) {
        if (!client.exists(id).exists) {
            return Optional.empty();
        }

        var spec = client.getModel(id);

        try (InputStream input = IOUtils.toInputStream(spec, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(IMappingSpecification.newBuilder().fromInputStream(input).build());
        } catch (IOException e) {
            // as we are reading from a string, it is highly unlikely that we hit an I/O error
            throw new RuntimeException(e);
        }

    }

}
