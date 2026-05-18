package org.tavall.control.guild;

public final class GuildStatModifierCalculationHandler implements IGuildDomain {
    public GuildStatModifierCalculationHandler() {
    }

    public GuildStatModifierCalculationHandler(GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        registerGuildJobBuffCalculationHandler(guildJobBuffCalculationHandler);
    }

    public double calculateFinalStat(GuildStatCalculationContext context) {
        double modifier = 0.0d;
        for (GuildUpgrade upgrade : context.upgrades()) {
            modifier += upgrade.level() * 0.05d;
        }
        for (GuildBuff buff : context.activeBuffs()) {
            if (buff.activeAt(context.now())) {
                modifier += buff.modifier();
            }
        }
        modifier += getGuildJobBuffCalculationHandler().calculateJobModifier(context.actor(), context.actionDomain());
        if (context.kingdomState() == KingdomState.PROTECTED) {
            modifier += 0.02d;
        }
        return context.baseStat() * (1.0d + modifier);
    }
}
