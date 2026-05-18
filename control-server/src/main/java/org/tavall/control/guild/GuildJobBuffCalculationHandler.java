package org.tavall.control.guild;

import java.util.EnumMap;
import java.util.Map;

public final class GuildJobBuffCalculationHandler {
    private static final Map<GuildJobTitle, GuildJobBuff> DEFAULT_BUFFS = defaultBuffs();

    public double calculateJobModifier(GuildMemberProfile memberProfile, GuildJobDomain domain) {
        if (memberProfile == null) {
            return 0.0d;
        }
        double modifier = 0.0d;
        for (GuildJobTitle jobTitle : memberProfile.jobTitles()) {
            GuildJobBuff buff = DEFAULT_BUFFS.get(jobTitle);
            if (buff != null && buff.domain() == domain) {
                modifier += buff.modifier();
            }
        }
        return modifier;
    }

    private static Map<GuildJobTitle, GuildJobBuff> defaultBuffs() {
        EnumMap<GuildJobTitle, GuildJobBuff> buffs = new EnumMap<>(GuildJobTitle.class);
        buffs.put(GuildJobTitle.TREASURER, new GuildJobBuff(GuildJobTitle.TREASURER, GuildJobDomain.ECONOMY, 0.10d));
        buffs.put(GuildJobTitle.GENERAL, new GuildJobBuff(GuildJobTitle.GENERAL, GuildJobDomain.WAR, 0.10d));
        buffs.put(GuildJobTitle.ARCHITECT, new GuildJobBuff(GuildJobTitle.ARCHITECT, GuildJobDomain.BUILDING, 0.10d));
        buffs.put(GuildJobTitle.SCOUT_MASTER, new GuildJobBuff(GuildJobTitle.SCOUT_MASTER, GuildJobDomain.SCOUTING, 0.10d));
        buffs.put(GuildJobTitle.DIPLOMAT, new GuildJobBuff(GuildJobTitle.DIPLOMAT, GuildJobDomain.DIPLOMACY, 0.10d));
        buffs.put(GuildJobTitle.QUARTERMASTER, new GuildJobBuff(GuildJobTitle.QUARTERMASTER, GuildJobDomain.LOGISTICS, 0.10d));
        buffs.put(GuildJobTitle.PROPAGANDIST, new GuildJobBuff(GuildJobTitle.PROPAGANDIST, GuildJobDomain.PROPAGANDA, 0.15d));
        return Map.copyOf(buffs);
    }
}
