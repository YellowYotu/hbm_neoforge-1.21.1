package com.yellowyotu.hbmneoforge.material;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public record HBMMaterialDefinition(String id, String displayName, int color, MaterialKind kind,
                                    Set<MaterialShape> shapes) {
    public HBMMaterialDefinition {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("material id is blank");
        shapes = Collections.unmodifiableSet(shapes.isEmpty() ? EnumSet.noneOf(MaterialShape.class) : EnumSet.copyOf(shapes));
    }
}
