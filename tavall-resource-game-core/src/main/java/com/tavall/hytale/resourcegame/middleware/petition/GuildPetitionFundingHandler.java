package com.tavall.hytale.resourcegame.middleware.petition;

public final class GuildPetitionFundingHandler {
    private final PetitionRepository petitionRepository;

    public GuildPetitionFundingHandler(PetitionRepository petitionRepository) {
        this.petitionRepository = petitionRepository;
    }

    public Petition fundPetition(PetitionId petitionId, long amount) {
        Petition petition = petitionRepository.findPetition(petitionId)
                .orElseThrow(() -> new PetitionValidationException("Petition was not found."));
        return petitionRepository.savePetition(petition.funded(amount));
    }
}
