package com.lahusa.arcane_auctions.util;

import net.minecraft.util.Mth;

public class ExperienceConverter {
    public static int getTotalXPRequiredToLevel(int currentLevel) {
        if (currentLevel <= 16) {
            return currentLevel*currentLevel + 6*currentLevel;
        }
        else if (currentLevel <= 31) {
            return Mth.floor(2.5 * currentLevel * currentLevel - 40.5 * currentLevel + 360);
        }
        else {
            return Mth.floor(4.5 * currentLevel * currentLevel - 162.5 * currentLevel + 2220);
        }
    }

    public static int getXPRequiredForNextLevel(int currentLevel) {
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

    public static int getTotalCurrentXPPoints(int currentLevel, float currentXPProgress) {
        return getTotalXPRequiredToLevel(currentLevel) + Math.round(getXPRequiredForNextLevel(currentLevel) * currentXPProgress);
    }

    public static float getLevelsAtXPPoints(int points) {
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
