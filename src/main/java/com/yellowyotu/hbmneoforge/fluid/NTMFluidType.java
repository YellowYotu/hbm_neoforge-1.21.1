package com.yellowyotu.hbmneoforge.fluid;

import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum NTMFluidType {
    WATER("water", 0x3333FF, FluidClass.LIQUID, 20, "water"),
    COOLANT("coolant", 0xd8fcff, FluidClass.LIQUID, 20, "coolant"),
    SULFURIC_ACID("sulfuric_acid", 0xB0AA64, FluidClass.CORROSIVE, 20, "sulfuric_acid"),
    NITRIC_ACID("nitric_acid", 0xF0E084, FluidClass.CORROSIVE, 20, "nitric_acid"),
    PEROXIDE("peroxide", 0xfff7aa, FluidClass.CORROSIVE, 20, "peroxide"),
    SOLVENT("solvent", 0xE4E3EF, FluidClass.CORROSIVE, 20, "solvent"),
    HELIUM4("helium4", 0xE54B0A, FluidClass.GAS, 20, "helium4"),
    PERFLUOROMETHYL("perfluoromethyl", 0xBDC8DC, FluidClass.LIQUID, 15, "perfluoromethyl"),
    PERFLUOROMETHYL_COLD("perfluoromethyl_cold", 0xA7D7EF, FluidClass.LIQUID, -180, "perfluoromethyl_cold"),
    STEAM("steam", 0xe5e5e5, FluidClass.GAS, 100, "steam"),
    AIR("air", 0xE7EAEB, FluidClass.GAS, 20, "air"),
    FLUE_GAS("flue_gas", 0x131313, FluidClass.GAS, 20, "flue"),
    HOTSTEAM("hotsteam", 0xE7D6D6, FluidClass.GAS, 300, "hotsteam"),
    SUPERHOTSTEAM("superhotsteam", 0xE7B7B7, FluidClass.GAS, 450, "superhotsteam"),
    ULTRAHOTSTEAM("ultrahotsteam", 0xE39393, FluidClass.GAS, 600, "ultrahotsteam"),
    LAVA("lava", 0xFF3300, FluidClass.LIQUID, 1200, "lava"),
    DEUTERIUM("deuterium", 0x0000FF, FluidClass.GAS, 20, "deuterium"),
    TRITIUM("tritium", 0x000099, FluidClass.GAS, 20, "tritium"),
    OIL("oil", 0x020202, FluidClass.LIQUID, 20, "oil"),
    HOTOIL("hotoil", 0x300900, FluidClass.LIQUID, 350, "hotoil"),
    HEAVYOIL("heavyoil", 0x141312, FluidClass.LIQUID, 20, "heavyoil"),
    BITUMEN("bitumen", 0x1f2426, FluidClass.LIQUID, 20, "bitumen"),
    SMEAR("smear", 0x190f01, FluidClass.LIQUID, 20, "smear"),
    HEATINGOIL("heatingoil", 0x211806, FluidClass.LIQUID, 20, "heatingoil"),
    RECLAIMED("reclaimed", 0x332b22, FluidClass.LIQUID, 20, "reclaimed"),
    PETROIL("petroil", 0x44413d, FluidClass.LIQUID, 20, "petroil"),
    LUBRICANT("lubricant", 0x606060, FluidClass.LIQUID, 20, "lubricant"),
    NAPHTHA("naphtha", 0x595744, FluidClass.LIQUID, 20, "naphtha"),
    DIESEL("diesel", 0xf2eed5, FluidClass.LIQUID, 20, "diesel"),
    LIGHTOIL("lightoil", 0x8c7451, FluidClass.LIQUID, 20, "lightoil"),
    KEROSENE("kerosene", 0xffa5d2, FluidClass.LIQUID, 20, "kerosene"),
    GAS("gas", 0xfffeed, FluidClass.GAS, 20, "gas"),
    PETROLEUM("petroleum", 0x7cb7c9, FluidClass.GAS, 20, "petroleum"),
    LPG("lpg", 0x4747EA, FluidClass.LIQUID, 20, "lpg"),
    BIOGAS("biogas", 0xbfd37c, FluidClass.GAS, 20, "biogas"),
    BIOFUEL("biofuel", 0xeef274, FluidClass.LIQUID, 20, "biofuel"),
    NITAN("nitan", 0x8018ad, FluidClass.LIQUID, 20, "nitan"),
    UF6("uf6", 0xD1CEBE, FluidClass.CORROSIVE, 20, "uf6"),
    PUF6("puf6", 0x4C4C4C, FluidClass.CORROSIVE, 20, "puf6"),
    SAS3("sas3", 0x4ffffc, FluidClass.CORROSIVE, 20, "sas3"),
    SCHRABIDIC("schrabidic", 0x006B6B, FluidClass.CORROSIVE, 20, "schrabidic"),
    AMAT("amat", 0x010101, FluidClass.GAS, 20, "amat"),
    ASCHRAB("aschrab", 0xb50000, FluidClass.GAS, 20, "aschrab"),
    WATZ("watz", 0x86653E, FluidClass.CORROSIVE, 20, "watz"),
    CRYOGEL("cryogel", 0x32ffff, FluidClass.LIQUID, -170, "cryogel"),
    HYDROGEN("hydrogen", 0x4286f4, FluidClass.LIQUID, -260, "hydrogen"),
    OXYGEN("oxygen", 0x98bdf9, FluidClass.LIQUID, -100, "oxygen"),
    XENON("xenon", 0xba45e8, FluidClass.GAS, 20, "xenon"),
    BALEFIRE("balefire", 0x28e02e, FluidClass.CORROSIVE, 1500, "balefire"),
    MERCURY("mercury", 0x808080, FluidClass.LIQUID, 20, "mercury"),
    PAIN("pain", 0x938541, FluidClass.CORROSIVE, 300, "pain"),
    WASTEFLUID("wastefluid", 0x544400, FluidClass.LIQUID, 20, "wastefluid"),
    WASTEGAS("wastegas", 0xB8B8B8, FluidClass.GAS, 20, "wastegas"),
    GASOLINE("gasoline", 0x445772, FluidClass.LIQUID, 20, "gasoline"),
    COALGAS("coalgas", 0x445772, FluidClass.LIQUID, 20, "coalgas"),
    SPENTSTEAM("spentsteam", 0x445772, FluidClass.GAS, 20, "spentsteam"),
    FRACKSOL("fracksol", 0x798A6B, FluidClass.CORROSIVE, 20, "fracksol"),
    PLASMA_DT("plasma_dt", 0xF7AFDE, FluidClass.GAS, 3250, "plasma_dt"),
    PLASMA_HD("plasma_hd", 0xF0ADF4, FluidClass.GAS, 2500, "plasma_hd"),
    PLASMA_HT("plasma_ht", 0xD1ABF2, FluidClass.GAS, 3000, "plasma_ht"),
    PLASMA_XM("plasma_xm", 0xC6A5FF, FluidClass.GAS, 4250, "plasma_xm"),
    PLASMA_BF("plasma_bf", 0xA7F1A3, FluidClass.GAS, 8500, "plasma_bf"),
    CARBONDIOXIDE("carbondioxide", 0x404040, FluidClass.GAS, 20, "carbondioxide"),
    PLASMA_DH3("plasma_dh3", 0xFF83AA, FluidClass.GAS, 3480, "plasma_dh3"),
    HELIUM3("helium3", 0xFCF0C4, FluidClass.GAS, 20, "helium3"),
    DEATH("death", 0x717A88, FluidClass.CORROSIVE, 300, "death"),
    ETHANOL("ethanol", 0xe0ffff, FluidClass.LIQUID, 20, "ethanol"),
    HEAVYWATER("heavywater", 0x00a0b0, FluidClass.LIQUID, 20, "heavywater"),
    CRACKOIL("crackoil", 0x020202, FluidClass.LIQUID, 20, "crackoil"),
    COALOIL("coaloil", 0x020202, FluidClass.LIQUID, 20, "coaloil"),
    HOTCRACKOIL("hotcrackoil", 0x300900, FluidClass.LIQUID, 350, "hotcrackoil"),
    NAPHTHA_CRACK("naphtha_crack", 0x595744, FluidClass.LIQUID, 20, "naphtha_crack"),
    LIGHTOIL_CRACK("lightoil_crack", 0x8c7451, FluidClass.LIQUID, 20, "lightoil_crack"),
    DIESEL_CRACK("diesel_crack", 0xf2eed5, FluidClass.LIQUID, 20, "diesel_crack"),
    AROMATICS("aromatics", 0x68A09A, FluidClass.LIQUID, 20, "aromatics"),
    UNSATURATEDS("unsaturateds", 0x628FAE, FluidClass.GAS, 20, "unsaturateds"),
    SALIENT("salient", 0x457F2D, FluidClass.LIQUID, 20, "salient"),
    XPJUICE("xpjuice", 0xBBFF09, FluidClass.LIQUID, 20, "xpjuice"),
    ENDERJUICE("enderjuice", 0x127766, FluidClass.LIQUID, 20, "enderjuice"),
    PETROIL_LEADED("petroil_leaded", 0x44413d, FluidClass.LIQUID, 20, "petroil_leaded"),
    GASOLINE_LEADED("gasoline_leaded", 0x445772, FluidClass.LIQUID, 20, "gasoline_leaded"),
    COALGAS_LEADED("coalgas_leaded", 0x445772, FluidClass.LIQUID, 20, "coalgas_leaded"),
    COOLANT_HOT("coolant_hot", 0x99525E, FluidClass.LIQUID, 600, "coolant_hot"),
    MUG("mug", 0x4B2D28, FluidClass.LIQUID, 20, "mug"),
    MUG_HOT("mug_hot", 0x6B2A20, FluidClass.LIQUID, 500, "mug_hot"),
    WOODOIL("woodoil", 0x847D54, FluidClass.LIQUID, 20, "woodoil"),
    COALCREOSOTE("coalcreosote", 0x51694F, FluidClass.LIQUID, 20, "coalcreosote"),
    SEEDSLURRY("seedslurry", 0x7CC35E, FluidClass.LIQUID, 20, "seedslurry"),
    BLOOD("blood", 0xB22424, FluidClass.LIQUID, 20, "blood"),
    BLOOD_HOT("blood_hot", 0xF22419, FluidClass.LIQUID, 666, "blood_hot"),
    SYNGAS("syngas", 0x131313, FluidClass.GAS, 20, "syngas"),
    OXYHYDROGEN("oxyhydrogen", 0x483FC1, FluidClass.GAS, 20, "oxyhydrogen"),
    RADIOSOLVENT("radiosolvent", 0xA4D7DD, FluidClass.CORROSIVE, 20, "radiosolvent"),
    HYDRAZINE("hydrazine", 0x31517D, FluidClass.CORROSIVE, 20, "hydrazine"),
    CHLORINE("chlorine", 0xBAB572, FluidClass.CORROSIVE, 20, "chlorine"),
    HEAVYOIL_VACUUM("heavyoil_vacuum", 0x131214, FluidClass.LIQUID, 20, "heavyoil_vacuum"),
    REFORMATE("reformate", 0x835472, FluidClass.LIQUID, 20, "reformate"),
    LIGHTOIL_VACUUM("lightoil_vacuum", 0x8C8851, FluidClass.LIQUID, 20, "lightoil_vacuum"),
    SOURGAS("sourgas", 0xC9BE0D, FluidClass.CORROSIVE, 20, "sourgas"),
    XYLENE("xylene", 0x5C4E76, FluidClass.LIQUID, 20, "xylene"),
    HEATINGOIL_VACUUM("heatingoil_vacuum", 0x211D06, FluidClass.LIQUID, 20, "heatingoil_vacuum"),
    DIESEL_REFORM("diesel_reform", 0xCDC3C6, FluidClass.LIQUID, 20, "diesel_reform"),
    DIESEL_CRACK_REFORM("diesel_crack_reform", 0xCDC3CC, FluidClass.LIQUID, 20, "diesel_crack_reform"),
    KEROSENE_REFORM("kerosene_reform", 0xFFA5F3, FluidClass.LIQUID, 20, "kerosene_reform"),
    REFORMGAS("reformgas", 0x6362AE, FluidClass.GAS, 20, "reformgas"),
    COLLOID("colloid", 0x787878, FluidClass.LIQUID, 20, "colloid"),
    PHOSGENE("phosgene", 0xCFC4A4, FluidClass.GAS, 20, "phosgene"),
    MUSTARDGAS("mustardgas", 0xBAB572, FluidClass.GAS, 20, "mustardgas"),
    IONGEL("iongel", 0xB8FFFF, FluidClass.LIQUID, 20, "iongel"),
    OIL_COKER("oil_coker", 0x001802, FluidClass.LIQUID, 20, "oil_coker"),
    NAPHTHA_COKER("naphtha_coker", 0x495944, FluidClass.LIQUID, 20, "naphtha_coker"),
    GAS_COKER("gas_coker", 0xDEF4CA, FluidClass.GAS, 20, "gas_coker"),
    EGG("egg", 0xD2C273, FluidClass.LIQUID, 20, "egg"),
    CHOLESTEROL("cholesterol", 0xD6D2BD, FluidClass.LIQUID, 20, "cholesterol"),
    ESTRADIOL("estradiol", 0xCDD5D8, FluidClass.LIQUID, 20, "estradiol"),
    FISHOIL("fishoil", 0x4B4A45, FluidClass.LIQUID, 20, "fishoil"),
    SUNFLOWEROIL("sunfloweroil", 0xCBAD45, FluidClass.LIQUID, 20, "sunfloweroil"),
    NITROGLYCERIN("nitroglycerin", 0x92ACA6, FluidClass.LIQUID, 20, "nitroglycerin"),
    REDMUD("redmud", 0xD85638, FluidClass.CORROSIVE, 20, "redmud"),
    CHLOROCALCITE_SOLUTION("chlorocalcite_solution", 0x808080, FluidClass.CORROSIVE, 20, "chlorocalcite_solution"),
    CHLOROCALCITE_MIX("chlorocalcite_mix", 0x808080, FluidClass.CORROSIVE, 20, "chlorocalcite_mix"),
    CHLOROCALCITE_CLEANED("chlorocalcite_cleaned", 0x808080, FluidClass.CORROSIVE, 20, "chlorocalcite_cleaned"),
    POTASSIUM_CHLORIDE("potassium_chloride", 0x808080, FluidClass.CORROSIVE, 20, "potassium_chloride"),
    CALCIUM_CHLORIDE("calcium_chloride", 0x808080, FluidClass.CORROSIVE, 20, "calcium_chloride"),
    CALCIUM_SOLUTION("calcium_solution", 0x808080, FluidClass.CORROSIVE, 20, "calcium_solution"),
    SMOKE("smoke", 0x808080, FluidClass.GAS, 20, "smoke"),
    SMOKE_LEADED("smoke_leaded", 0x808080, FluidClass.GAS, 20, "smoke_leaded"),
    SMOKE_POISON("smoke_poison", 0x808080, FluidClass.GAS, 20, "smoke_poison"),
    HEAVYWATER_HOT("heavywater_hot", 0x4D007B, FluidClass.LIQUID, 600, "heavywater_hot"),
    SODIUM("sodium", 0xCCD4D5, FluidClass.LIQUID, 400, "sodium"),
    SODIUM_HOT("sodium_hot", 0xE2ADC1, FluidClass.LIQUID, 1200, "sodium_hot"),
    THORIUM_SALT("thorium_salt", 0x7A5542, FluidClass.CORROSIVE, 800, "thorium_salt"),
    THORIUM_SALT_HOT("thorium_salt_hot", 0x3E3627, FluidClass.CORROSIVE, 1600, "thorium_salt_hot"),
    THORIUM_SALT_DEPLETED("thorium_salt_depleted", 0x302D1C, FluidClass.CORROSIVE, 800, "thorium_salt_depleted"),
    FULLERENE("fullerene", 0xFF7FED, FluidClass.CORROSIVE, 20, "fullerene"),
    PHEROMONE("pheromone", 0x5FA6E8, FluidClass.LIQUID, 20, "pheromone"),
    PHEROMONE_M("pheromone_m", 0x48C9B0, FluidClass.LIQUID, 20, "pheromone_m"),
    OIL_DS("oil_ds", 0x121212, FluidClass.LIQUID, 20, "oil_ds"),
    HOTOIL_DS("hotoil_ds", 0x3F180F, FluidClass.LIQUID, 350, "hotoil_ds"),
    CRACKOIL_DS("crackoil_ds", 0x2A1C11, FluidClass.LIQUID, 20, "crackoil_ds"),
    HOTCRACKOIL_DS("hotcrackoil_ds", 0x3A1A28, FluidClass.LIQUID, 350, "hotcrackoil_ds"),
    NAPHTHA_DS("naphtha_ds", 0x63614E, FluidClass.LIQUID, 20, "naphtha_ds"),
    LIGHTOIL_DS("lightoil_ds", 0x63543E, FluidClass.LIQUID, 20, "lightoil_ds"),
    STELLAR_FLUX("stellar_flux", 0xE300FF, FluidClass.GAS, 20, "stellar_flux"),
    VITRIOL("vitriol", 0x6E5222, FluidClass.LIQUID, 20, "vitriol"),
    SLOP("slop", 0x929D45, FluidClass.LIQUID, 20, "slop"),
    LEAD("lead", 0x666672, FluidClass.LIQUID, 350, "lead"),
    LEAD_HOT("lead_hot", 0x776563, FluidClass.LIQUID, 1500, "lead_hot"),
    PERFLUOROMETHYL_HOT("perfluoromethyl_hot", 0xB899DE, FluidClass.LIQUID, 250, "perfluoromethyl_hot"),
    LYE("lye", 0xFFECCC, FluidClass.CORROSIVE, 20, "lye"),
    SODIUM_ALUMINATE("sodium_aluminate", 0xFFD191, FluidClass.CORROSIVE, 20, "sodium_aluminate"),
    BAUXITE_SOLUTION("bauxite_solution", 0xE2560F, FluidClass.CORROSIVE, 20, "bauxite_solution"),
    ALUMINA("alumina", 0xDDFFFF, FluidClass.LIQUID, 20, "alumina"),
    CONCRETE("concrete", 0xA2A2A2, FluidClass.LIQUID, 20, "concrete"),
    AIRBLAST("airblast", 0xFFDADA, FluidClass.GAS, 20, "airblast");

    public enum FluidClass { LIQUID, GAS, CORROSIVE }

    private final String id;
    private final int color;
    private final FluidClass fluidClass;
    private final int temperature;
    private final String icon;

    NTMFluidType(String id, int color, FluidClass fluidClass, int temperature, String icon) {
        this.id = id;
        this.color = color;
        this.fluidClass = fluidClass;
        this.temperature = temperature;
        this.icon = icon;
    }

    public String id() { return id; }
    public int color() { return color; }
    public FluidClass fluidClass() { return fluidClass; }
    public int temperature() { return temperature; }
    public boolean isGas() { return fluidClass == FluidClass.GAS; }
    public boolean isCorrosive() { return fluidClass == FluidClass.CORROSIVE; }
    public ResourceLocation iconTexture() { return ResourceLocation.fromNamespaceAndPath(HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, "textures/gui/fluids/" + icon + ".png"); }
    public Component displayName() { return Component.translatable("fluid.hbm_neoforge." + id); }
    public Component bracketedName() { return Component.literal("[").append(displayName()).append("]").withStyle(ChatFormatting.AQUA); }

    public static NTMFluidType byId(String id) {
        if (id == null) {
            return null;
        }
        if (id.equals("flue")) {
            return FLUE_GAS;
        }
        for (NTMFluidType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    public static NTMFluidType byOrdinalSafe(int ordinal) {
        NTMFluidType[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
    }
}
