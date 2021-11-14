package apoy2k.robby.model

import org.apache.commons.lang3.RandomStringUtils

enum class PowerUp {
    RAMMING_GEAR,
    DUAL_PROCESSOR,
    RADIO_CONTROL,
    SUPERIOR_ARCHIVE,
    POWER_DOWN_SHIELD,
    ABLATIVE_COAT,
    GYROSCOPIC_STABILIZER,
    FLYWHEEL,
    FOURTH_GEAR,
    REAR_FACING_LASER,
    HIGH_POWER_LASER,
    MINI_HOWITZER,
    SCRAMBLER,
    CONDITIONAL_PROGRAM,
    PRESSOR_BEAM,
    ABORT_SWITCH,
    BRAKES,
    CRAB_LEGS,
    DOUBLE_BARRELED_LASER,
    MECHANICAL_ARM,
    TRACTOR_BEAM,
    RECOMPILE,
    EXTRA_MEMORY,
    REVERSE_GEAR,
    CIRCUIT_BREAKER,
    FIRE_CONTROL
}

data class ModificationCard(val powerUp: PowerUp, val description: String? = "") {
    val id: String = RandomStringUtils.randomAlphanumeric(5)

    override fun toString() = "ModificationCard($id, $powerUp)"
}
