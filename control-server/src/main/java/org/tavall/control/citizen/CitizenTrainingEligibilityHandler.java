package org.tavall.control.citizen;

import org.tavall.dependency.IDependencyInjectableConcrete;

public final class CitizenTrainingEligibilityHandler implements CitizenDomain, IDependencyInjectableConcrete {
    public CitizenTrainingEligibilityHandler() {
    }

    public CitizenTrainingEligibilityHandler(CitizenLifeStageEligibilityHandler lifeStageEligibilityHandler) {
        registerCitizenDependency(CitizenLifeStageEligibilityHandler.class, lifeStageEligibilityHandler);
    }

    public void requireCanStartTraining(CitizenData citizen) {
        if (!getCitizenLifeStageEligibilityHandler().canTrain(citizen.ageStage())) {
            throw new CitizenValidationException("Citizen age stage cannot start training: " + citizen.ageStage() + ".");
        }
        if (citizen.status() != CitizenStatus.ACTIVE_CITIZEN) {
            throw new CitizenValidationException("Only active citizens can start training.");
        }
        if (citizen.healthState() == CitizenHealthState.WOUNDED || citizen.healthState() == CitizenHealthState.CRITICAL) {
            throw new CitizenValidationException("Wounded or critical citizens cannot start training.");
        }
        if (citizen.moraleState() == CitizenMoraleState.POOR) {
            throw new CitizenValidationException("Poor morale blocks training.");
        }
        if (citizen.nutritionState() == CitizenNutritionState.STARVING) {
            throw new CitizenValidationException("Starving citizens cannot start training.");
        }
    }

    public void requireCanPromote(CitizenData citizen) {
        if (!getCitizenLifeStageEligibilityHandler().canPromoteToTroop(citizen.ageStage())) {
            throw new CitizenValidationException("Citizen age stage cannot promote to troop: " + citizen.ageStage() + ".");
        }
        if (citizen.status() != CitizenStatus.IN_TRAINING) {
            throw new CitizenValidationException("Citizen must be in training before promotion.");
        }
        if (citizen.trainingState() == CitizenTrainingState.UNTRAINED) {
            throw new CitizenValidationException("Citizen is not trained enough for troop promotion.");
        }
        if (citizen.healthState() == CitizenHealthState.WOUNDED || citizen.healthState() == CitizenHealthState.CRITICAL) {
            throw new CitizenValidationException("Wounded or critical citizens cannot promote to troop.");
        }
    }
}
