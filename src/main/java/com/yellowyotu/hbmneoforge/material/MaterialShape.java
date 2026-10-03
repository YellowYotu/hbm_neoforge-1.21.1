package com.yellowyotu.hbmneoforge.material;

public enum MaterialShape {
    QUANTUM("quantum", 1), NUGGET("nugget", 8), TINY("tiny", 8), FRAGMENT("fragment", 8),
    TINY_POWDER("powder_tiny", 8), WIRE("wire", 9), BOLT("bolt", 9), BILLET("billet", 48),
    INGOT("ingot", 72), GEM("gem", 72), CRYSTAL("crystal", 72), DUST("powder", 72),
    DENSE_WIRE("wire_dense", 72), PLATE("plate", 72), CAST_PLATE("plate_cast", 216),
    WELDED_PLATE("plate_welded", 432), SHELL("shell", 288), PIPE("pipe", 216), BLOCK("block", 648),
    HEAVY_COMPONENT("heavy_component", 55_296), PART("part", 3), LIGHT_BARREL("barrel_light", 216),
    HEAVY_BARREL("barrel_heavy", 432), LIGHT_RECEIVER("receiver_light", 288),
    HEAVY_RECEIVER("receiver_heavy", 648), MECHANISM("mechanism", 288), STOCK("stock", 288), GRIP("grip", 144);

    private final String id;
    private final int units;

    MaterialShape(String id, int units) { this.id = id; this.units = units; }
    public String id() { return id; }
    public int units() { return units; }
}
