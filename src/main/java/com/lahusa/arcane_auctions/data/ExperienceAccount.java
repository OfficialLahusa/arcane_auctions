package com.lahusa.arcane_auctions.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class ExperienceAccount {
    public String name;
    public UUID owner;
    public int balance;

    public ExperienceAccount() {
        balance = 0;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("name", name);
        tag.putUUID("owner", owner);
        tag.putInt("balance", balance);
        return tag;
    }

    public void load(CompoundTag tag) {
        name = tag.getString("name");
        owner = tag.getUUID("owner");
        balance = tag.getInt("balance");
    }
}
