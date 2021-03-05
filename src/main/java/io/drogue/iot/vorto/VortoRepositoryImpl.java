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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.cache.CacheResult;

@Singleton
public class VortoRepositoryImpl implements VortoRepository {

    private static final Logger LOG = LoggerFactory.getLogger(VortoRepositoryImpl.class);

    @Inject
    @RestClient
    VortoClient client;

    @CacheResult(cacheName = "mapping-cache")
    @Override
    public Optional<IMappingSpecification> getMappingByModelId(final String id) {
        if (!client.mappingExists(id).exists) {
            return Optional.empty();
        }

        var spec = client.getMapping(id);

        LOG.debug("Spec:\n{}", spec);

        try (InputStream input = IOUtils.toInputStream(spec, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(IMappingSpecification.newBuilder().fromInputStream(input).build());
        } catch (IOException e) {
            // as we are reading from a string, it is highly unlikely that we hit an I/O error
            throw new RuntimeException(e);
        }

    }

}
