package io.drogue.iot.vorto;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

import org.eclipse.vorto.mapping.engine.model.spec.IMappingSpecification;

import io.quarkus.test.Mock;

@Mock
@Singleton
public class MockVortoRepository implements VortoRepository {

    private final Map<String, IMappingSpecification> mappings;

    public MockVortoRepository() throws IOException {
        this.mappings = new HashMap<>();

        try (InputStream input = MockVortoRepository.class.getResourceAsStream("/DeviceOne.json")) {
            this.mappings.put("vorto.private.ctron:DeviceOne:1.0.0", IMappingSpecification.newBuilder().fromInputStream(input).build());
        }
    }

    @Override
    public Optional<IMappingSpecification> getById(String id) {
        return Optional.ofNullable(this.mappings.get(id));
    }

}
