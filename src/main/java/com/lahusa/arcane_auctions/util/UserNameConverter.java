package com.lahusa.arcane_auctions.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

public class UserNameConverter {
    private static final HashMap<UUID, String> _userCache;

    static {
        _userCache = new HashMap<>();
    }

    public static String getUserName(UUID uuid){
        if (!_userCache.containsKey(uuid)) {
            String name = fetchUserName(uuid);
            _userCache.put(uuid, name);
        }
        return _userCache.get(uuid);
    }

    public static void clearCache() {
        _userCache.clear();
    }

    private static String fetchUserName(UUID uuid) {
        String url = "https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "")+"/names";
        try {
            String json = IOUtils.toString(new URL(url));
            JsonElement element = new JsonParser().parse(json);
            JsonArray nameArray = element.getAsJsonArray();
            JsonObject nameElement = nameArray.get(nameArray.size()-1).getAsJsonObject();
            return nameElement.get("name").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Not Found";
    }
}
