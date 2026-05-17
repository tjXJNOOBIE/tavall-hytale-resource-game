package com.tavall.resourcegame.middleware.authority;

public enum AuthorityCondition {
    APPROVED_ONLY,
    BREAK_GLASS_ONLY,
    NON_PRODUCTION_ONLY,
    BUSINESS_HOURS_ONLY,
    CONTROL_PLANE_UNREACHABLE,
    LOW_RISK_ONLY,
    HUMAN_APPROVAL_REQUIRED
}
