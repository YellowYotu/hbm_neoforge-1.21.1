package com.yellowyotu.hbmneoforge.material;

public record MaterialForm(HBMMaterialDefinition material, MaterialShape shape) {
    public String registryName() {
        return switch (shape) {
            case TINY_POWDER -> "powder_" + material.id() + "_tiny";
            case CAST_PLATE -> "plate_" + material.id() + "_cast";
            case WELDED_PLATE -> "plate_" + material.id() + "_welded";
            default -> shape.id() + "_" + material.id();
        };
    }
}
