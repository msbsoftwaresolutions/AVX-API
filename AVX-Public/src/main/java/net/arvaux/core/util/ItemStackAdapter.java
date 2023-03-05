package net.arvaux.core.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    private static final String ENCHANTMENT_MEMBER = "enchantments";
    private static final String AMOUNT_MEMBER = "amount";
    private static final String DATA_MEMBER = "data";
    private static final String LORE_MEMBER = "lore";
    private static final String TYPE_MEMBER = "mat";
    private static final String TITLE_MEMBER = "title";
    private static final String SKULL_OWNER_MEMBER = "skullOwner";
    private static final String SKULL_OWNER_CUSTOM_MEMBER = "skullCustomTexture";
    private static final String UNBREAKABLE_MEMBER = "unbreakable";
    private static final String COLOR_MEMBER = "color";
    private static final String POTIONS_MEMBER = "potions";

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement == null || jsonElement.isJsonNull() || !jsonElement.isJsonObject()) {
            return null;
        }

        JsonObject object = jsonElement.getAsJsonObject();

        Material material = Material.getMaterial(object.get(TYPE_MEMBER).getAsString());
        int amount = object.get(AMOUNT_MEMBER).getAsInt();
        short data = object.get(DATA_MEMBER).getAsShort();

        ItemStack stack = new ItemStack(material, amount, data);

        if (object.has(SKULL_OWNER_CUSTOM_MEMBER)) {
            stack = SkullTexture.createSkull(object.get(SKULL_OWNER_CUSTOM_MEMBER).getAsString());
        }

        if (object.has(ENCHANTMENT_MEMBER)) {
            Map<Enchantment, Integer> enchants = Maps.newHashMap();
            JsonObject enchantsObject = object.get(ENCHANTMENT_MEMBER).getAsJsonObject();
            enchantsObject.entrySet().forEach(entry -> enchants.put(Enchantment.getByName(entry.getKey()), entry.getValue().getAsInt()));
            stack.addUnsafeEnchantments(enchants);
        }

        ItemMeta meta = stack.getItemMeta();

        if (object.has(TITLE_MEMBER)) {
            meta.setDisplayName(object.get(TITLE_MEMBER).getAsString());
        }

        if (object.has(LORE_MEMBER)) {
            JsonArray loreArray = object.get(LORE_MEMBER).getAsJsonArray();
            List<String> lore = Lists.newArrayList();
            for (int i = 0; i < loreArray.size(); i++) {
                lore.add(loreArray.get(i).getAsString());
            }

            meta.setLore(lore);
        }

        if (object.has(POTIONS_MEMBER) && meta instanceof PotionMeta) {
            JsonArray effects = object.get(POTIONS_MEMBER).getAsJsonArray();
            for (int i = 0; i < effects.size(); i++) {
                JsonObject effect = effects.get(i).getAsJsonObject();

                PotionEffectType potionType = PotionEffectType.getByName(effect.get("type").getAsString());
                int duration = effect.get("duration").getAsInt();
                int amplifier = effect.get("amplifier").getAsInt();
                boolean ambient = effect.get("ambient").getAsBoolean();
                boolean particles = effect.get("particles").getAsBoolean();

                ((PotionMeta) meta).addCustomEffect(new PotionEffect(potionType, duration, amplifier, ambient, particles), true);
            }
        }

        if (object.has(SKULL_OWNER_MEMBER) && meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(object.get(SKULL_OWNER_MEMBER).getAsString());
        }

        if (object.has(COLOR_MEMBER)) {
            if (object.get(COLOR_MEMBER).isJsonObject()) {
                JsonObject color = object.get(COLOR_MEMBER).getAsJsonObject();
                int red = UtilMath.clamp(color.get("red").getAsInt(), 0, 255);
                int green = UtilMath.clamp(color.get("green").getAsInt(), 0, 255);
                int blue = UtilMath.clamp(color.get("blue").getAsInt(), 0, 255);
                ((LeatherArmorMeta) meta).setColor(Color.fromRGB(red, green, blue));
            } else {
                int colorRGB = object.get(COLOR_MEMBER).getAsInt();
                ((LeatherArmorMeta) meta).setColor(Color.fromRGB(colorRGB));
            }
        }

        if (object.has(UNBREAKABLE_MEMBER)) {
            meta.spigot().setUnbreakable(object.get(UNBREAKABLE_MEMBER).getAsBoolean());
        }

        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public JsonElement serialize(ItemStack stack, Type type, JsonSerializationContext jsonSerializationContext) {
        if (stack == null) {
            return JsonNull.INSTANCE;
        }

        JsonObject object = new JsonObject();
        object.addProperty(TYPE_MEMBER, stack.getType().name());
        object.addProperty(AMOUNT_MEMBER, stack.getAmount());
        object.addProperty(DATA_MEMBER, stack.getDurability());

        JsonObject enchants = new JsonObject();
        stack.getEnchantments().forEach((enchant, level) -> enchants.addProperty(enchant.getName(), level));
        if (enchants.entrySet().size() > 0) {
            object.add(ENCHANTMENT_MEMBER, enchants);
        }

        if (!stack.hasItemMeta()) {
            return object;
        }

        ItemMeta meta = stack.getItemMeta();

        if (meta.hasDisplayName()) {
            object.addProperty(TITLE_MEMBER, meta.getDisplayName());
        }

        if (meta.hasLore()) {
            JsonArray lore = new JsonArray();
            for (String loreLine : meta.getLore()) {
                lore.add(new JsonPrimitive(loreLine));
            }

            object.add(LORE_MEMBER, lore);
        }

        if (meta instanceof PotionMeta) {
            List<PotionEffect> effectList = ((PotionMeta) meta).getCustomEffects();
            JsonArray effects = new JsonArray();
            effectList.forEach(effect -> {
                JsonObject effectObject = new JsonObject();

                effectObject.addProperty("type", effect.getType().getName());
                effectObject.addProperty("duration", effect.getDuration());
                effectObject.addProperty("amplifier", effect.getAmplifier());
                effectObject.addProperty("ambient", effect.isAmbient());
                effectObject.addProperty("particles", effect.hasParticles());

                effects.add(effectObject);
            });

            object.add(POTIONS_MEMBER, effects);
        }

        if (meta instanceof SkullMeta) {
            String skullCustomTexture = SkullTexture.getTexture((SkullMeta) meta);
            if (skullCustomTexture == null) {
                String skullOwner = ((SkullMeta) meta).getOwner();
                object.addProperty(SKULL_OWNER_MEMBER, skullOwner);
            } else {
                object.addProperty(SKULL_OWNER_CUSTOM_MEMBER, skullCustomTexture);
            }
        }

        if (meta instanceof LeatherArmorMeta && ((LeatherArmorMeta) meta).getColor() != null) {
            Color color = ((LeatherArmorMeta) meta).getColor();
            object.add(COLOR_MEMBER, new JsonPrimitive(color.asRGB()));
        }

        object.addProperty(UNBREAKABLE_MEMBER, meta.spigot().isUnbreakable());
        return object;
    }
}
