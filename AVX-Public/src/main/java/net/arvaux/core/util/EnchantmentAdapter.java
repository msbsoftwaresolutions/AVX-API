package net.arvaux.core.util;

import com.google.gson.*;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Type;

public class EnchantmentAdapter implements JsonDeserializer<Enchantment>, JsonSerializer<Enchantment> {
    @Override
    public Enchantment deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Enchantment.getByName(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(Enchantment enchantment, Type type, JsonSerializationContext jsonSerializationContext) {
        return new Gson().toJsonTree(enchantment.getName());
    }
}
