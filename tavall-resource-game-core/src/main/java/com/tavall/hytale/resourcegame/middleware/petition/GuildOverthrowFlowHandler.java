package com.tavall.hytale.resourcegame.middleware.petition;

public final class GuildOverthrowFlowHandler {
    public boolean canResolveOverthrowImmediately(Petition petition, boolean explicitPolicyEnabled) {
        return explicitPolicyEnabled
                && petition.petitionType() == PetitionType.REMOVE_LEADER
                && petition.state() == PetitionState.VIRAL;
    }
}
