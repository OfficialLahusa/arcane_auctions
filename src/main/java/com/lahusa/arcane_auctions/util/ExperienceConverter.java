package com.lahusa.arcane_auctions.util;

import net.minecraft.util.Mth;

public class ExperienceConverter {
    public static long getTotalXPRequiredToLevel(long currentLevel) {
        if (currentLevel <= 16) {
            return currentLevel*currentLevel + 6L*currentLevel;
        }
        else if (currentLevel <= 31) {
            return (long)(2.5 * currentLevel * currentLevel - 40.5 * currentLevel + 360);
        }
        else {
            return (long)(4.5 * currentLevel * currentLevel - 162.5 * currentLevel + 2220);
        }
    }

    public static long getXPRequiredForNextLevel(long currentLevel) {
        if (currentLevel <= 15) {
            return 2 * currentLevel + 7;
        }
        else if (currentLevel <= 30) {
            return 5 * currentLevel - 38;
        }
        else {
            return 9 * currentLevel - 158;
        }
    }

    public static long getTotalCurrentXPPoints(long currentLevel, float currentXPProgress) {
        return getTotalXPRequiredToLevel(currentLevel) + Math.round(getXPRequiredForNextLevel(currentLevel) * currentXPProgress);
    }

    public static float getLevelsAtXPPoints(long points) {
        if (points <= 0) {
            return 0;
        }
        else if (points <= 352) {
            return Mth.sqrt(points + 9) - 3;
        }
        else if (points <= 1507) {
            return 8.1f + Mth.sqrt(0.4f * (points - (7839f / 40f)));
        }
        else {
            return (325f/18f) + Mth.sqrt((2f/9f) * (points - (54215f / 72f)));
        }
    }
}
