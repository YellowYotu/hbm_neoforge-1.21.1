# HBM CE Material Pipeline Parity

## Goal

Replace the current approximate material-form generation with a source-of-truth catalog derived from the bundled HBM CE reference. Every exposed material form must use the correct CE asset and participate in every CE processing path that the port can execute. JEI must show those paths with their actual inputs, outputs, amounts, fluids, chances, machine, and intermediate products.

## Scope

This work covers every material present in HBM CE, including materials not yet registered in the NeoForge port. A material is imported as a complete supported family rather than as isolated placeholder items.

The importer/catalog records, per material:

- CE registry identity and display name;
- whether a vanilla Minecraft item or block is the canonical form;
- every form that CE actually registers, such as fragment, nugget, ingot, billet, powder, tiny powder, plate, cast plate, welded plate, wire, dense wire, pipe, shell, scrap, molten form, and storage block;
- the exact CE texture/model source for each non-vanilla form;
- all acquisition, conversion, recycling, melting, casting, welding, and machine-processing recipes;
- radiation or other traits only when the original item has them.

Bedrock ore fragments and forms that do not exist in CE remain excluded.

## Canonical identity and vanilla reuse

Vanilla forms are aliases, not duplicated HBM items. Iron, gold, copper, redstone, diamond, emerald, coal, and other matching vanilla forms use the corresponding `minecraft:` registry entries wherever CE semantics permit. HBM-specific forms remain separate only when CE provides a distinct item or behavior.

Recipes, tags, and JEI entries resolve through a canonical-form lookup. This prevents parallel items such as a generated HBM iron block beside `minecraft:iron_block`.

## Assets

Assets are copied or adapted from the bundled `.reference/ntm-ce` tree by an auditable mapping. Generic substitutes, unrelated icons, and placeholder textures such as food or vanilla items are forbidden.

Every registered non-vanilla form must have:

- an item model;
- its exact CE texture or an explicitly documented composite model matching CE;
- English, Russian, and Ukrainian names;
- a valid creative-tab and JEI presentation when obtainable.

Placeable blocks additionally require a blockstate, block model, texture, loot table, mining tags, and matching block item. Materials without a CE storage block do not receive one.

## Processing graph

Recipes are represented as a material-processing graph instead of independent guessed JSON files. Nodes are canonical item, fluid, and material forms. Edges correspond to an actual CE operation.

Supported operations include:

- crafting and reverse storage-block conversion;
- anvil construction, smithing, and recycling;
- shredder and rock mill crushing;
- furnace and machine smelting;
- crucible melting and molten-material storage;
- casting into every supported mold;
- arc-welder joining and welded forms;
- plate, wire, dense-wire, billet, nugget, powder, tiny-powder, fragment, and scrap conversions;
- probabilistic outputs with their original CE chances;
- any additional already-ported machine category referenced by CE.

An operation is enabled only when its required machine and behavior exist in the port. Missing machine functionality is reported by validation and the recipe is not presented as executable. Once that machine is implemented, the same catalog can expose its waiting CE edges without inventing new item definitions.

## JEI behavior

JEI categories are generated from the same runtime recipe data used by machines. Each ingredient and output receives its own slot. Fluid inputs and outputs show their exact millibucket amount. Chance outputs display the CE probability. Tool and mold catalysts appear separately from consumed inputs. Multi-step routes are discoverable by following usages and recipes for every intermediate form.

There must be no display-only recipe that the corresponding machine cannot execute, and no executable machine recipe missing from JEI.

## Migration and compatibility

Existing correctly named registry entries are retained where possible to protect worlds. Incorrect generated duplicates are removed from creative tabs and recipe graphs first. Registry removals are limited to entries proven not to exist in CE and not used as a canonical port form. Where removing an entry would damage existing saves, it remains as a hidden compatibility alias and converts to the canonical form.

## Validation

Automated tests build the full catalog and fail on:

- a CE form missing from the port without an explicit unsupported-machine reason;
- a form invented by the port but absent from CE;
- a placeholder or mismatched texture;
- a missing model, translation, blockstate, loot table, or mining tag;
- a recipe referencing an unregistered item, block, fluid, machine, mold, or tool;
- an obtainable material form with no incoming production path;
- a reversible CE conversion missing its reverse path;
- a runtime recipe absent from JEI or a JEI recipe absent from runtime;
- incorrect counts, fluid amounts, energy values, durations, or output chances.

The implementation is complete when the generated audit contains no unexplained gaps and the complete Gradle test suite passes.
