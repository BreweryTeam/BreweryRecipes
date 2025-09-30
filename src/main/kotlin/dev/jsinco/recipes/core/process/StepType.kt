package dev.jsinco.recipes.core.process

enum class StepType {

    MIX, COOK, DISTILL, AGE;

    companion object {
        @JvmStatic
        fun fromString(type: String?): StepType {
            if (type == null) return COOK
            return when (type.trim().lowercase()) {
                "distill" -> DISTILL
                "mix" -> MIX
                "age" -> AGE
                else -> COOK
            }
        }
    }
}