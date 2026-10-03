# Canister and Oil Refininery JEI Design

## Goal

Port the HBM CE fluid canister into the NeoForge project and make both the canister recipe and Oil Refinery recipes discoverable and usable through JEI. HBM CE is the authority for names, capacity, crafting shape, ingredient counts, textures, and refinery recipe values. The user's explicit extension is that every `NTMFluidType` available in this port receives a filled canister variant.

## Source of Truth

- Empty canister recipe: HBM CE `CraftingManager`, yielding two canisters from one steel plate and four aluminium plates in the pattern `S `/`AA`/`AA`.
- Canister behavior: HBM CE `ItemCanister`, with a capacity of `1000 mB`, an empty base texture, a tinted fluid overlay, and one filled subtype per supported fluid.
- Refinery values: HBM CE `RefineryRecipes` and `TileEntityMachineRefinery`.
- If the current port disagrees with these sources, the HBM CE values win unless NeoForge 1.21.1 requires a mechanical adaptation.

## Item Model

Register two item identities:

- `canister_empty`: the craftable empty container.
- `canister_full`: the filled container implemented with `ItemPortableFluidContainer` semantics and a fixed `1000 mB` capacity.

The filled item stores its `NTMFluidType` and amount in item component data. JEI and the creative tab expose a full `1000 mB` stack for every value returned by `NTMFluidType.values()`, excluding only the absence sentinel if one exists. The item name appends the localized fluid name, and its tooltip displays `1000 / 1000 mB` plus the existing original-style fluid description.

The item model uses the original empty canister texture as the base layer and the original canister overlay as a tintable second layer. The overlay color comes from `NTMFluidType.color()`. Empty and filled canisters must have distinct inventory appearances.

## Crafting

Add a shaped vanilla recipe under `data/hbm_neoforge/recipe/crafting`:

```text
S 
AA
AA
```

- `S`: `hbm_neoforge:plate_steel`
- `A`: `hbm_neoforge:plate_aluminium`
- result: two `hbm_neoforge:canister_empty`

The recipe must load through Minecraft's recipe manager and therefore appear automatically in JEI's crafting category. Its JSON format must match Minecraft/NeoForge 1.21.1 (`result.id` and `result.count`).

## Fluid Transfer

The filled canister participates in the existing portable-container flow used by fluid tanks and machines:

- A full canister transfers exactly `1000 mB` and returns one empty canister.
- A machine may fill an empty canister only when at least `1000 mB` of a single fluid is available.
- Partial transfers are rejected, preventing fluid loss or duplication.
- Machine slot validation accepts the new empty and filled canister items wherever other portable fluid containers are accepted.

## JEI Integration

Register subtype handling for `canister_full` using the stored fluid ID. Register all full canister variants as extra JEI ingredients and add them to the existing fluid-container recipe category so users can navigate between a fluid and its canister.

Add and register an `OilRefineryRecipeCategory`, its recipes, the Oil Refinery catalyst, and the refinery screen click area. The category reads directly from `OilRefineryRecipe.RECIPES` so runtime and JEI cannot drift apart. It displays:

- the `100 mB` input fluid;
- four output fluids and their exact quantities;
- the byproduct item when present;
- `5 HE/t` consumption;
- the original byproduct interval of one item per 100 completed operations;
- animated refinery progress and fluid columns based on the original GUI layout.

Invalid or missing refinery filters produce no recipe. Removing an installed filter does not reset the previously configured input fluid.

## Recipe Reliability

Tests must verify that the canister recipe JSON uses registered item IDs and the 1.21.1 result schema. A resource-level test checks that every project recipe references existing project items or known tags. The runtime log must contain no recipe parse failures after resources reload.

The existing refinery recipe registry is the shared data source for processing and JEI. Tests assert all four HBM CE recipes, input/output quantities, energy cost, and byproduct interval.

## Validation

- Unit tests for canister capacity, saved fluid identity, supported variant enumeration, and full-only transfer rules.
- Unit tests for exact refinery quantities and filter-removal persistence.
- Resource checks for the shaped recipe, item models, textures, localization keys, and JEI registration.
- `gradlew test` and `gradlew compileJava` must pass.
- In-game verification confirms the recipe crafts two canisters, JEI shows all filled variants, the refinery category opens from the machine, and fluid tooltips show correct names and quantities.

## Out of Scope

This design does not add new fluid types, change refinery geometry, or alter pipe connection positions. Full refinery runtime processing and sound changes remain a separate implementation stage unless they are already required to make the registered refinery recipes operational.
