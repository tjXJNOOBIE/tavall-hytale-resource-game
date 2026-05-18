package org.tavall.control.services;

import org.tavall.control.domain.FocusedWorldTarget;

/**
 * Scored target candidate used by focus resolution.
 */
public final class FocusCandidate {
    private final FocusedWorldTarget target;
    private final double score;

    public FocusCandidate(FocusedWorldTarget target, double score) {
        this.target = target;
        this.score = score;
    }

    public FocusedWorldTarget target() {
        return target;
    }

    public double score() {
        return score;
    }
}
