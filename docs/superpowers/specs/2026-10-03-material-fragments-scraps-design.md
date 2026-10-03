# HBM CE Complete Material Forms and Scraps Design

## Goal

Port the complete HBM CE material-form system needed by every resource already present in this NeoForge project. Every registered form must match the original HBM CE material declaration, have its original acquisition or conversion route, and appear in JEI. Recipes in Rock Mill, Foundry, Shredder, Anvil, Arc Furnace, Arc Welder, and other ported machines must never reference an item that is not registered. Per-material Bedrock Ore Fragment families remain excluded.

## Source of Truth

The bundled HBM CE archive under `.reference/ntm-ce` is authoritative. The implementation follows:

- `Mats` for each material's `setAutogen(...)` shape list;
- `MaterialShapes` for shape names and material-unit values;
- `ModItems` and `OreDictManager` for original item IDs, display names, and tags;
- the original machine recipe registries for inputs, outputs, amounts, chances, tiers, and machine requirements;
- `ItemScraps` for material scraps and additive scraps.

When current port data and HBM CE disagree, HBM CE wins unless Minecraft 1.21.1 requires a technical adaptation. Such adaptations may change storage or registration code, but not visible names, quantities, probabilities, or processing behavior.

## Central Material Catalog

Create one data-driven material catalog for every HBM CE material represented by a resource, fluid, recipe, or foundry entry in this port. A material definition contains:

- stable material ID and localized HBM CE name;
- material color and foundry properties;
- whether it is smeltable, additive, or non-castable;
- the exact set of HBM CE autogen shapes;
- aliases/tags needed by vanilla or NeoForge recipes;
- links to original processing recipes.

The catalog registers concrete NeoForge item IDs during startup. Data-driven registration removes copy-pasted declarations while preserving ordinary item IDs for recipes, commands, inventory search, and JEI.

The implementation ports the actual `setAutogen(...)` matrix. It does not give every shape to every material. Dense Wire exists only for materials declaring `DENSEWIRE`, and ordinary Wire is not invented merely to serve as an intermediate.

## Supported Material Shapes

The catalog supports all HBM CE shapes required by materials present in this port:

- Nugget;
- Fragment;
- Tiny Pile of Powder;
- Powder;
- Ingot;
- Gem and Crystal where declared;
- Wire;
- Bolt;
- Billet;
- Dense Wire;
- Plate, Cast Plate, and Welded Plate;
- Shell and Pipe;
- Block;
- machine or weapon components such as Heavy Component, Part, barrels, receivers, mechanism, stock, and grip when the original material declaration and an obtainable recipe both exist.

`QUANTUM` and other special internal shapes are registered only when an existing ported recipe or item uses them. No blank placeholder item is added solely to complete an enum.

All nine dedicated HBM CE fragment items are included:

- Neodymium;
- Cobalt;
- Niobium;
- Cerium;
- Lanthanium;
- Actinium;
- Meteorite;
- Boron;
- Coltan.

Materials that use the generic ore-fragment form in HBM CE receive it only when their original declaration includes `FRAGMENT`. Existing unrelated fission fragments are unchanged. Per-material Bedrock Ore Fragment items are not added.

## Registration, Assets, and Localization

Each generated concrete form receives:

- a stable item registration;
- the matching HBM CE English and Russian display name;
- model and texture references using the closest original HBM CE asset;
- correct NeoForge material tags;
- a creative-tab entry beside the corresponding material family;
- JEI subtype or ingredient handling where item data changes its identity.

The build contains a catalog validation test that fails on duplicate IDs, missing translations, missing model/texture resources, invalid tags, or a declared form without an acquisition path.

## Original Acquisition and Conversion Routes

Every material form uses its HBM CE route and quantities. The implementation audits and ports the relevant recipes from the original registries, including:

- ore and fragment crushing in Rock Mill or Shredder;
- fragment-to-powder and powder consolidation;
- tiny-powder composition and decomposition;
- smelting and arc-furnace processing;
- Foundry melting and casting with the original mold and material-unit cost;
- Dense Wire casting for materials that declare it;
- Arc Welder recipes only where HBM CE explicitly defines them;
- Anvil recipes, including result quantities and independent chances;
- block, ingot, nugget, powder, billet, plate, wire, and component conversions;
- scrap recovery and remelting.

Recipes that look counterintuitive remain unchanged if they are present in HBM CE. The completion report lists those recipes explicitly for manual game verification.

No substitute crafting-table recipe is invented when HBM CE requires a machine. If a required original machine already exists in the port, its recipe is implemented and shown in JEI. If the original route requires a machine that is genuinely absent from the port, the item remains unregistered unless an already ported original route can obtain it. This prevents creative-only dead items.

## Machine-Recipe Integrity Audit

Add a registry-level audit covering every loaded machine recipe. For each item and fluid input/output, the audit verifies that the referenced registry entry exists. It reports the recipe ID, machine type, and missing registry ID, then fails automated tests.

The initial repair covers all existing broken references, with special attention to Rock Mill recipes whose outputs were listed before their material forms existed. Runtime recipe loaders must not silently replace an unknown output with air, an empty stack, or a generic placeholder.

## Dynamic Material Scraps

Replace the placeholder material-specific scraps as the primary representation with one component-backed item equivalent to HBM CE `ItemScraps`. A scrap stack stores:

- material identifier;
- exact material-unit amount;
- whether the material is solid or molten.

JEI and the creative inventory expose a variant for every smeltable or additive material in the material catalog. Solid stacks display `<Material> Scraps`. Molten smeltable stacks use the liquid-scrap appearance. Molten additives use the original `Additive Scraps` name and appearance and show the original warning that the contents are an additive and cannot be cast directly.

Breaking a crucible, casting container, or other applicable foundry component preserves its exact material and material-unit amount in the dropped scrap stack. Smeltable scraps return to the molten-material network without loss. Additive scraps return only to compatible additive recipes and cannot be cast into shapes. The unrelated generic recycling item named `Scrap` remains separate.

The old Bismuth/material placeholder scraps may remain as compatibility aliases for existing worlds, but new recipes and drops use the component-backed scrap item. A migration path converts old placeholder stacks without losing their material identity.

## Rare Earth Chunk Anvil Recipe

Keep the tier-2 Steel Anvil recipe from HBM CE. One Rare Earth Ore Chunk produces these independent rolls:

- Boron Fragment ×1 guaranteed;
- Boron Fragment ×1 at `50%`;
- Lanthanium Fragment ×1 at `10%`;
- Cobalt Fragment ×1 guaranteed;
- Cobalt Fragment ×1 at `50%`;
- Cerium Fragment ×1 at `10%`;
- Neodymium Fragment ×1 at `50%`;
- Niobium Fragment ×1 at `50%`.

Taking the main output awards every successful extra roll. Results enter the player's inventory or drop beside the anvil when the inventory is full.

## JEI Presentation

JEI uses the same runtime recipe objects and displays every original route for the generated forms. Each category shows exact inputs, outputs, fluids, amounts, machine tier, and probabilities.

The Anvil category renders all eight Rare Earth result entries. Guaranteed and probabilistic copies remain separate because they are independent rolls. Hovering a result shows its exact chance.

The material catalog supplies JEI with every registered form and every component-backed scrap variant. Recipes with unusual original inputs or outputs remain visible and are not normalized into more intuitive alternatives.

## Creative Inventory Organization

Move Oil Refinery from `ORES_AND_BLOCKS` to `MACHINES`. Material forms and scraps remain in the resource or parts tab beside related materials and follow the original HBM CE grouping as closely as the current tabs allow.

## Error Handling and Compatibility

- Invalid or unknown material data on an old scrap stack displays an explicit `Unknown Material Scraps` name and cannot enter processing recipes.
- Recipe loading reports exact missing item/fluid IDs instead of silently skipping them.
- Existing valid item IDs are retained where possible so worlds and recipes continue to load.
- Renamed or replaced placeholder items receive missing-mapping or conversion handling.
- The catalog is deterministic so client and server register the same IDs in the same order.

## Validation

- Compare the catalog's forms against the HBM CE `Mats#setAutogen(...)` declarations for every in-scope material.
- Assert all nine dedicated fragment IDs, models, textures, translations, and acquisition paths exist.
- Assert every registered form has at least one valid original acquisition or conversion route.
- Assert every loaded machine recipe references registered item and fluid IDs; specifically cover all Rock Mill inputs and outputs.
- Assert the Rare Earth recipe has exactly eight outputs with the original quantities and probabilities.
- Assert JEI exposes all eight anvil outputs and their chance text.
- Assert Foundry molds consume the original material-unit amount and produce the correct concrete form.
- Assert Dense Wire exists only for materials that declare `DENSEWIRE`.
- Assert an ordinary Wire or Arc Welder route is never invented for a material lacking it in HBM CE.
- Assert a material scrap round trip preserves material ID, state, and exact material-unit amount.
- Assert Additive Scraps cannot be cast but remain consumable by compatible additive recipes.
- Assert every smeltable/additive catalog material has a scrap variant.
- Assert old material-scrap placeholders migrate or resolve safely.
- Assert Oil Refinery appears in `MACHINES` and no longer appears in `ORES_AND_BLOCKS`.
- Run unit tests and Java compilation, then inspect resource loading for missing models, textures, translations, tags, or recipe parse failures.

## Out of Scope

This work does not add per-material Bedrock Ore Fragment families, invent forms absent from HBM CE, create fake acquisition recipes, or port entirely new machines solely to make a form obtainable. Oil Refinery runtime processing and unrelated machine behavior remain governed by their own approved specifications.
