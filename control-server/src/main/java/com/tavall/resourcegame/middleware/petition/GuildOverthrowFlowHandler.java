package org.tavall.control.petition;

public final class GuildOverthrowFlowHandler implements IPetitionDomain {
    public boolean canResolveOverthrowImmediately(Petition petition, boolean explicitPolicyEnabled) {
        return explicitPolicyEnabled
                && petition.petitionType() == PetitionType.REMOVE_LEADER
                && petition.state() == PetitionState.VIRAL;
    }
}
