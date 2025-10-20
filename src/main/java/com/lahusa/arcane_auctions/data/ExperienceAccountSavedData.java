package com.lahusa.arcane_auctions.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ExperienceAccountSavedData extends SavedData {

    private final ArrayList<ExperienceAccount> _accounts;

    private ExperienceAccountSavedData() {
        _accounts = new ArrayList<>();
    }

    public static ExperienceAccountSavedData create() {
        return new ExperienceAccountSavedData();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag accountList = new ListTag();

        for (ExperienceAccount account : _accounts) {
            CompoundTag accountTag = new CompoundTag();
            account.save(accountTag);
            accountList.add(accountTag);
        }

        tag.put("accounts", accountList);

        return tag;
    }

    public static ExperienceAccountSavedData load(CompoundTag tag) {
        ExperienceAccountSavedData data = create();

        ListTag accountList = (ListTag) tag.get("accounts");

        assert accountList != null;

        for (Tag accountTag : accountList) {
            ExperienceAccount account = new ExperienceAccount();
            account.load((CompoundTag) accountTag);

            data._accounts.add(account);
        }

        return data;
    }

    public static ExperienceAccountSavedData getOrCreate(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ExperienceAccountSavedData::load, ExperienceAccountSavedData::create, "arcane-auctions-accounts");
    }
}
