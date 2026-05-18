package org.tavall.control.petition;

public final class GuildPetitionFundingHandler implements IPetitionDomain {
    public GuildPetitionFundingHandler() {
    }

    public GuildPetitionFundingHandler(PetitionRepository petitionRepository) {
        registerPetitionRepository(petitionRepository);
    }

    public Petition fundPetition(PetitionId petitionId, long amount) {
        Petition petition = getPetitionRepository().findPetition(petitionId)
                .orElseThrow(() -> new PetitionValidationException("Petition was not found."));
        return getPetitionRepository().savePetition(petition.funded(amount));
    }
}
