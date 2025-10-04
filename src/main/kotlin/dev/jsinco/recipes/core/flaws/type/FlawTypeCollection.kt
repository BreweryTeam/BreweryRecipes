package dev.jsinco.recipes.core.flaws.type

enum class FlawTypeCollection(vararg val flawTypes: FlawType) {

    ELDRITCH(ObfuscationFlawType, ReplacementFlawType(" "), ReplacementFlawType("  ")),
    UNCERTAIN(ReplacementFlawType("?"), InaccuracyFlawType, CorrectionFlawType);
}