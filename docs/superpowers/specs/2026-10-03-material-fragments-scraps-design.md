# HBM CE Fragments, Material Forms, and Scraps Design

## Goal

Port the complete dedicated fragment and material-scrap system required by the current NeoForge project, using HBM CE as the source of truth. Every registered form must have its original acquisition or conversion route and must be visible in JEI, even when an original recipe looks unusual. Bedrock Ore Fragment variants are excluded.

## Source of Truth and Scope

The authoritative sources are HBM CE `ModItems`, `Mats`, `OreDictManager`, `ShredderRecipes`, foundry and arc-furnace recipes, Arc Welder recipes, and anvil recipes. Names, quantities, probabilities, material-unit costs, and machine requirements follow those sources.

Add all nine dedicated HBM CE fragments:

- Neodymium
- Cobalt
- Niobium
- Cerium
- Lanthanium
- Actinium
- Meteorite
- Boron
- Coltan

The system does not generate one Bedrock Ore Fragment item for every material. Existing unrelated fission fragments are outside this scope.

## Material Forms

For each of the nine fragment materials, register every form that has a real HBM CE production route and whose required machine exists in this port. Relevant forms include Fragment, Tiny Pile of Powder, Powder, Ingot, Nugget, Billet, Block, Wire, and Dense Wire. A form is omitted when HBM CE has no corresponding form or no original production path can run with the machines currently available.

In particular, Dense Wire is added only for materials declared with the HBM CE `DENSEWIRE` shape. Neodymium and Niobium receive Dense Wire and their original foundry mold routes. Ordinary Wire is not invented as an intermediate when HBM CE does not declare it. Arc Welder recipes are added only where HBM CE explicitly defines them.

Fragment-to-powder conversion follows HBM CE Shredder recipes. Casting uses the existing `FoundryMaterialRegistry` material-unit values and molds. Smelting, consolidation, and decomposition recipes retain original quantities. Every registered item must be reachable from at least one valid recipe or world source.

## Dynamic Material Scraps

Replace the three placeholder scrap items as the primary representation with one component-backed material scrap item equivalent to HBM CE `ItemScraps`. A scrap stack stores:

- material identifier;
- exact material-unit amount;
- whether the contents are already molten.

JEI and the creative inventory expose a variant for every smeltable or additive material present in the port's foundry material registry. The display name is `<Material> Scraps` for solid scraps. A molten smeltable stack displays the material name and uses the liquid-scrap appearance. A molten additive uses the original `Additive Scraps` appearance and tooltip warning that it is an additive and cannot be cast directly.

Scraps produced by breaking a crucible, casting container, or other applicable foundry component preserve the exact stored material and amount. Smeltable scraps can be returned to the molten-material network without loss. Additive scraps can return to recipes that accept the additive but cannot produce a cast shape directly. Generic `Scrap` used by recycling recipes remains separate from material scraps.

The implementation provides material scraps for every smeltable/additive resource known to the port, not only Bismuth and the fragment materials. Names use the HBM CE material localization keys and the original `"%s Scraps"` naming structure.

## Rare Earth Chunk Anvil Recipe

Keep the single tier-2 Steel Anvil recipe from HBM CE. One Rare Earth Ore Chunk produces these independent rolls:

- Boron Fragment ×1 guaranteed;
- Boron Fragment ×1 at `50%`;
- Lanthanium Fragment ×1 at `10%`;
- Cobalt Fragment ×1 guaranteed;
- Cobalt Fragment ×1 at `50%`;
- Cerium Fragment ×1 at `10%`;
- Neodymium Fragment ×1 at `50%`;
- Niobium Fragment ×1 at `50%`.

Taking the main output awards every successful extra roll. Items enter the player's inventory or drop beside the anvil when inventory space is unavailable.

## JEI Presentation

The Anvil category renders all eight Rare Earth result entries and gives each probabilistic entry a chance tooltip. Guaranteed and probabilistic copies of the same fragment remain separate, matching the original roll model.

All Fragment, Shredder, powder consolidation, smelting, foundry casting, scrap recovery, and Dense Wire recipes are registered in their appropriate JEI categories. Machine recipes use the same data objects as runtime processing so displayed quantities cannot drift from actual consumption and output.

If an HBM CE recipe looks counterintuitive, it is still ported unchanged. The completion report will list these unusual recipes explicitly so the user can verify them in game.

## Creative Inventory Organization

Move Oil Refinery from `ORES_AND_BLOCKS` to the `MACHINES` creative tab. Fragment forms and material scraps remain in the resource/parts tabs in the same relative grouping as their related materials.

## Validation

- Assert all nine dedicated fragment IDs, models, textures, and localization entries exist.
- Assert every registered material form has at least one valid acquisition/conversion recipe.
- Assert the Rare Earth recipe has exactly eight outputs with the original quantities and probabilities.
- Assert JEI exposes all eight anvil outputs and their chance text.
- Assert a material scrap round trip preserves material identity and material-unit amount.
- Assert Additive Scraps cannot be cast but remain consumable by compatible additive recipes.
- Assert every smeltable/additive foundry material has a scrap variant.
- Assert Oil Refinery occurs in `MACHINES` and no longer occurs in `ORES_AND_BLOCKS`.
- Run the full unit-test suite and Java compilation, then inspect the game resource log for missing models, textures, translations, items, or recipe parse failures.

## Out of Scope

Do not add Bedrock Ore Fragment families, invent material forms absent from HBM CE, or add placeholder recipes merely to make an otherwise unreachable item appear obtainable.
