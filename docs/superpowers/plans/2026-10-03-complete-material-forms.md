# Complete HBM CE Material Forms Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add every original HBM CE material form and acquisition route required by resources already present in the port, repair missing machine-recipe references, add material-aware scraps, and expose the complete system in JEI.

**Architecture:** A central immutable material catalog mirrors HBM CE `Mats#setAutogen(...)` and drives item registration, foundry behavior, assets, tags, creative variants, and recipe validation. Concrete form items retain stable registry IDs; only material scraps use item data because their identity includes a material, state, and arbitrary unit amount. Machine recipes remain their own runtime source of truth and JEI reads those same recipe objects.

**Tech Stack:** Java 21, NeoForge 1.21.1 deferred registries and data components, Minecraft JSON resources, JEI 19 API, JUnit 5, Gradle.

**Spec:** `docs/superpowers/specs/2026-10-03-material-fragments-scraps-design.md`

## Global Constraints

- `.reference/ntm-ce` is authoritative for material shapes, names, quantities, probabilities, tiers, and machine routes.
- Register only shapes declared by the material's original `setAutogen(...)` definition.
- Do not invent ordinary Wire, Dense Wire, Arc Welder, crafting-table, or other substitute routes.
- Do not add per-material Bedrock Ore Fragment families.
- Every registered form must have an original obtainable route through a machine already present in this port.
- Keep existing registry IDs where possible and provide compatibility handling for replaced scrap placeholders.
- JEI must render the same recipe objects used by runtime processing.

## Review Focus

- A recipe references an unknown item or fluid: validation names the recipe and missing ID and fails instead of producing an empty stack.
- A material declares Dense Wire but no ordinary Wire: Dense Wire casting works without inventing the missing Wire form.
- A scrap stack contains an unknown or removed material ID: it is visibly invalid and cannot enter foundry processing.
- A recipe has repeated guaranteed and probabilistic outputs of the same item: JEI keeps every independent roll and shows each chance.
- Client/server catalog order differs: registration remains deterministic by stable material and shape order.

---

### Task 1: Extract and Lock the HBM CE Material Matrix

**Files:**
- Create: `src/main/java/com/yellowyotu/hbmneoforge/material/MaterialShape.java`
- Create: `src/main/java/com/yellowyotu/hbmneoforge/material/HBMMaterialDefinition.java`
- Create: `src/main/java/com/yellowyotu/hbmneoforge/material/HBMMaterialCatalog.java`
- Create: `src/test/java/com/yellowyotu/hbmneoforge/material/HBMMaterialCatalogTest.java`

**Interfaces:**
- Produces: `MaterialShape` with original unit amount and registry-name fragment.
- Produces: `HBMMaterialDefinition(String id, String displayName, int color, MaterialKind kind, Set<MaterialShape> shapes)`.
- Produces: `HBMMaterialCatalog.all()`, `get(String)`, and deterministic `forms()` APIs used by every later task.

- [ ] **Step 1: Write failing catalog tests**

Add tests named `containsEveryInScopeHbmMaterial`, `matchesRepresentativeAutogenShapes`, `containsAllNineDedicatedFragments`, and `catalogOrderIsDeterministic`. Pin representative shape sets for Copper, Gold, Titanium, Tungsten, Neodymium, Niobium, Cobalt, Boron, and Lanthanium against `.reference/ntm-ce/.../Mats.java`; assert no material produces a Bedrock Ore Fragment form.

- [ ] **Step 2: Run the catalog test and verify failure**

Run: `.\gradlew.bat test --tests "*HBMMaterialCatalogTest" --no-daemon`

Expected: FAIL because the catalog types do not exist.

- [ ] **Step 3: Implement the shape enum, immutable definition, and exact in-scope catalog**

Transcribe the original shape matrix from HBM CE for every material represented by a current resource, fluid, recipe, or foundry entry. Preserve original material-unit values and use stable ordered collections.

- [ ] **Step 4: Run the catalog test**

Run: `.\gradlew.bat test --tests "*HBMMaterialCatalogTest" --no-daemon`

Expected: PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: add HBM material catalog`

### Task 2: Register Concrete Material Forms

**Files:**
- Create: `src/main/java/com/yellowyotu/hbmneoforge/material/MaterialFormRegistry.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/ModItems.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/HbmNeoForge.java`
- Create: `src/test/java/com/yellowyotu/hbmneoforge/material/MaterialFormRegistryTest.java`

**Interfaces:**
- Consumes: `HBMMaterialCatalog.forms()` from Task 1.
- Produces: `MaterialFormRegistry.item(materialId, shape)`, `allItems()`, and `registryName(materialId, shape)`.

- [ ] **Step 1: Write failing registration-contract tests**

Assert stable IDs for representative forms, all nine dedicated fragments, uniqueness of every generated ID, absence of forms not declared by the catalog, and deterministic ordering. Add the Review Focus test proving Niobium/Neodymium Dense Wire does not require an invented ordinary Wire item.

- [ ] **Step 2: Run the focused test and verify failure**

Run: `.\gradlew.bat test --tests "*MaterialFormRegistryTest" --no-daemon`

Expected: FAIL because generated registrations are absent.

- [ ] **Step 3: Implement startup-time concrete item registration**

Register each catalog form through the existing deferred item registry, preserve current IDs as aliases/constants where already exposed, and provide explicit lookup failures rather than returning `Items.AIR`.

- [ ] **Step 4: Run focused tests and Java compilation**

Run: `.\gradlew.bat test --tests "*MaterialFormRegistryTest" --no-daemon`

Run: `.\gradlew.bat compileJava --no-daemon`

Expected: both PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: register complete material forms`

### Task 3: Generate and Validate Material Assets, Names, and Tags

**Files:**
- Create: `src/main/java/com/yellowyotu/hbmneoforge/datagen/MaterialFormItemModelProvider.java`
- Create: `src/main/java/com/yellowyotu/hbmneoforge/datagen/MaterialFormLanguageProvider.java`
- Create: `src/main/java/com/yellowyotu/hbmneoforge/datagen/MaterialFormTagProvider.java`
- Create: `src/main/java/com/yellowyotu/hbmneoforge/datagen/ModDataGenerators.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/HBMsNuclearTechModUnofficialNeoForgeEdition.java`
- Modify/Create: `src/main/resources/assets/hbm_neoforge/textures/item/material/**`
- Modify: `src/main/resources/assets/hbm_neoforge/lang/en_us.json`
- Modify: `src/main/resources/assets/hbm_neoforge/lang/ru_ru.json`
- Create: `src/test/java/com/yellowyotu/hbmneoforge/material/MaterialResourceCoverageTest.java`

**Interfaces:**
- Consumes: form IDs and definitions from Tasks 1–2.
- Produces: item models, original HBM CE English/Russian names, conventional material tags, and a resource coverage validator.

- [ ] **Step 1: Write failing resource-coverage tests**

For every concrete form, require an English translation, Russian translation, model, resolvable texture, and expected item tag. Detect duplicate translation keys and missing texture targets.

- [ ] **Step 2: Run the resource test and verify it reports missing assets**

Run: `.\gradlew.bat test --tests "*MaterialResourceCoverageTest" --no-daemon`

Expected: FAIL listing concrete missing resources.

- [ ] **Step 3: Add data providers and port the closest original HBM CE textures**

Generate uniform model/tag files from the catalog, add original localized names, and copy/adapt original textures without replacing distinct HBM forms with blank placeholders.

- [ ] **Step 4: Generate data and run resource coverage**

Run: `.\gradlew.bat runData --no-daemon`

Run: `.\gradlew.bat test --tests "*MaterialResourceCoverageTest" --no-daemon`

Expected: PASS with no missing model, texture, translation, or tag.

- [ ] **Step 5: Commit**

Commit message: `feat: add material form assets and names`

### Task 4: Port Original Acquisition Routes and Repair Recipe References

**Files:**
- Create: `src/main/java/com/yellowyotu/hbmneoforge/recipe/MachineRecipeRegistryAudit.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/blockentity/RockMillRecipes.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/blockentity/ShredderRecipes.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/blockentity/HBMAnvilRecipes.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/blockentity/MachinePressRecipes.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/blockentity/BlastFurnaceRecipes.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/blockentity/ArcWelderRecipes.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/blockentity/MachineRecipeJsonLoader.java`
- Modify/Create: `src/main/resources/data/hbm_neoforge/machine_recipe/**`
- Modify/Create: `src/main/resources/data/hbm_neoforge/recipe/**`
- Create: `src/test/java/com/yellowyotu/hbmneoforge/recipe/MaterialAcquisitionRecipeTest.java`
- Create: `src/test/java/com/yellowyotu/hbmneoforge/recipe/MachineRecipeRegistryAuditTest.java`

**Interfaces:**
- Consumes: concrete form lookup from Task 2.
- Produces: `MachineRecipeRegistryAudit.validate(RegistryAccess, Collection<LoadedMachineRecipe>) -> List<RecipeReferenceError>`.

- [ ] **Step 1: Write failing acquisition and integrity tests**

Assert every registered form has at least one original route; pin original quantities for representative fragment, powder, tiny powder, nugget, ingot, block, billet, wire, and plate conversions. Add tests that inject missing item/fluid IDs and require an error containing recipe ID, machine type, and missing ID. Cover every current Rock Mill output.

- [ ] **Step 2: Run the recipe tests and verify failures**

Run: `.\gradlew.bat test --tests "*MaterialAcquisitionRecipeTest" --tests "*MachineRecipeRegistryAuditTest" --no-daemon`

Expected: FAIL on missing routes and unresolved current recipe references.

- [ ] **Step 3: Port exact HBM CE routes and integrate strict audit**

Use the original quantities, chances, tiers, and machine types. Remove silent AIR/empty-stack fallbacks. Do not add a form whose only original route requires a machine absent from this port.

- [ ] **Step 4: Run focused tests**

Run: `.\gradlew.bat test --tests "*MaterialAcquisitionRecipeTest" --tests "*MachineRecipeRegistryAuditTest" --no-daemon`

Expected: PASS, including all Rock Mill references.

- [ ] **Step 5: Commit**

Commit message: `feat: port material processing recipes`

### Task 5: Integrate Material Forms with the Foundry

**Files:**
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/foundry/FoundryMaterialRegistry.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/foundry/FoundryTransfer.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/blockentity/FoundryCastingBlockEntity.java`
- Create: `src/test/java/com/yellowyotu/hbmneoforge/foundry/FoundryMaterialFormsTest.java`

**Interfaces:**
- Consumes: material properties and form lookup from Tasks 1–2.
- Produces: catalog-backed melting lookup and `getMoldResult(ItemStack mold, String material)` with exact HBM CE units.

- [ ] **Step 1: Write failing foundry matrix tests**

Pin unit values for Nugget, Wire, Billet, Ingot, Plate, Dense Wire, Cast Plate, Shell, Pipe, and Block. Assert each declared castable form resolves, undeclared forms do not resolve, and Dense Wire works without ordinary Wire.

- [ ] **Step 2: Run foundry tests and verify failure**

Run: `.\gradlew.bat test --tests "*FoundryMaterialFormsTest" --no-daemon`

Expected: FAIL on incomplete material/color/form mappings.

- [ ] **Step 3: Replace name-probing with catalog-backed foundry lookup**

Keep the existing public mold-result behavior while resolving concrete items from `MaterialFormRegistry`; port all in-scope colors, smeltable/additive flags, and original unit amounts.

- [ ] **Step 4: Run foundry tests**

Run: `.\gradlew.bat test --tests "*FoundryMaterialFormsTest" --no-daemon`

Expected: PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: connect material catalog to foundry`

### Task 6: Add Component-Backed Material Scraps

**Files:**
- Create: `src/main/java/com/yellowyotu/hbmneoforge/item/MaterialScrapsItem.java`
- Create: `src/main/java/com/yellowyotu/hbmneoforge/material/MaterialScrapData.java`
- Create: `src/main/java/com/yellowyotu/hbmneoforge/ModDataComponents.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/ModItems.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/block/CrucibleBlock.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/blockentity/CrucibleBlockEntity.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/block/FoundryCastingBlock.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/blockentity/FoundryCastingBlockEntity.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/HBMsNuclearTechModUnofficialNeoForgeEdition.java`
- Create: `src/test/java/com/yellowyotu/hbmneoforge/material/MaterialScrapsTest.java`

**Interfaces:**
- Consumes: catalog material ID/kind and foundry transfer APIs.
- Produces: `MaterialScrapData(String materialId, int units, ScrapState state)` codec/stream codec and `MaterialScrapsItem.create(...)`.

- [ ] **Step 1: Write failing scrap round-trip and safety tests**

Assert material ID, exact units, and solid/molten state survive serialization; smeltable scraps recover without loss; additive scraps cannot cast; unknown material data displays `Unknown Material Scraps` and cannot process; legacy Bismuth/additive placeholders map safely.

- [ ] **Step 2: Run scrap tests and verify failure**

Run: `.\gradlew.bat test --tests "*MaterialScrapsTest" --no-daemon`

Expected: FAIL because typed scrap data is absent.

- [ ] **Step 3: Implement the component, item variants, remelting, drops, and compatibility mapping**

Use one registered scrap item for new stacks, retain generic recycling `Scrap`, and preserve exact material units when foundry containers break.

- [ ] **Step 4: Run scrap and foundry tests**

Run: `.\gradlew.bat test --tests "*MaterialScrapsTest" --tests "*FoundryMaterialFormsTest" --no-daemon`

Expected: PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: add material-aware scraps`

### Task 7: Show Complete Material Processing in JEI

**Files:**
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/compat/jei/HBMJeiPlugin.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/compat/jei/AnvilRecipeCategory.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/compat/jei/RockMillRecipeCategory.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/compat/jei/ShredderRecipeCategory.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/compat/jei/FoundryCastingRecipes.java`
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/compat/jei/FoundryCastingRecipeCategory.java`
- Create: `src/test/java/com/yellowyotu/hbmneoforge/compat/jei/MaterialJeiPresentationTest.java`

**Interfaces:**
- Consumes: runtime machine recipes, form items, and scrap variants from Tasks 2, 4, and 6.
- Produces: complete JEI registration and pure layout/chance helpers testable without a client.

- [ ] **Step 1: Write failing JEI presentation tests**

Assert all eight Rare Earth results remain separate in order with `100/50/10/100/50/10/50/50%`; every runtime material route reaches its JEI category; every scrap variant is exposed; unusual original recipes retain their exact values.

- [ ] **Step 2: Run JEI tests and verify failure**

Run: `.\gradlew.bat test --tests "*MaterialJeiPresentationTest" --no-daemon`

Expected: FAIL because Anvil renders only the primary result and material variants are incomplete.

- [ ] **Step 3: Register all routes and render multi-output chances**

Drive JEI from runtime recipes, add material-scrap subtype identity/variants, and show exact chance tooltips without merging repeated result items.

- [ ] **Step 4: Run JEI tests and compile**

Run: `.\gradlew.bat test --tests "*MaterialJeiPresentationTest" --no-daemon`

Run: `.\gradlew.bat compileJava --no-daemon`

Expected: PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: expose material recipes in JEI`

### Task 8: Organize Creative Tabs and Run End-to-End Validation

**Files:**
- Modify: `src/main/java/com/yellowyotu/hbmneoforge/ModCreativeTabs.java`
- Create: `src/test/java/com/yellowyotu/hbmneoforge/material/MaterialSystemIntegrationTest.java`
- Modify: `docs/superpowers/plans/2026-10-03-complete-material-forms.md` only to check completed steps during execution.

**Interfaces:**
- Consumes: all earlier task outputs.
- Produces: final creative-tab ordering and whole-system verification.

- [ ] **Step 1: Write failing integration tests**

Assert Oil Refinery appears only in Machines; each concrete form appears exactly once in the intended resources/parts grouping; each smeltable/additive material exposes one scrap variant; all catalog forms have resources, an acquisition route, valid machine references, and a JEI presentation.

- [ ] **Step 2: Run integration tests and verify failure**

Run: `.\gradlew.bat test --tests "*MaterialSystemIntegrationTest" --no-daemon`

Expected: FAIL until creative ordering and any remaining cross-system gaps are fixed.

- [ ] **Step 3: Move Oil Refinery and populate catalog-driven creative entries**

Keep related families adjacent and avoid duplicate entries from legacy constants.

- [ ] **Step 4: Run all automated checks**

Run: `.\gradlew.bat test --no-daemon`

Run: `.\gradlew.bat compileJava --no-daemon`

Run: `.\gradlew.bat runData --no-daemon`

Expected: all commands PASS with no missing registry, recipe, model, texture, tag, or translation errors.

- [ ] **Step 5: Perform a development-client smoke test**

Launch the client, inspect material families and scraps in creative/JEI, inspect all eight Rare Earth Anvil outputs, verify representative Rock Mill/Shredder/Foundry routes, and confirm Oil Refinery is in Machines. Record unusual recipes copied unchanged from HBM CE for the completion report.

- [ ] **Step 6: Commit**

Commit message: `feat: complete HBM material system`
