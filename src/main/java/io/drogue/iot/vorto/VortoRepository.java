package io.drogue.iot.vorto;

import java.util.Optional;

import org.eclipse.vorto.mapping.engine.model.spec.IMappingSpecification;

public interface VortoRepository {
    Optional<IMappingSpecification> getById(String id);
}
