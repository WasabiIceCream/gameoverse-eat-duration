package net.gameoverse.eatduration;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Foods that heal more take longer to eat. Hearty Meals heals a food's
 * nutrition value in half-hearts, so nutrition is the scale. Items without
 * a food component (potions, milk) are left alone.
 */
public final class EatDuration implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("gameoverse_eat_duration");

    @Override
    public void onInitialize() {
        EatDurationConfig.get();
        PayloadTypeRegistry.clientboundPlay().register(SyncPayload.TYPE, SyncPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.player, SyncPayload.TYPE)) {
                sender.sendPacket(SyncPayload.of(EatDurationConfig.get()));
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> writeReport());
    }

    /** The eating time for this stack, given the time its item would otherwise use. */
    public static int scale(ItemStack stack, int ticks) {
        if (ticks <= 0) {
            return ticks;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            return ticks;
        }
        EatDurationConfig config = EatDurationConfig.get();
        double seconds = ticks / 20.0;
        double scaled = seconds * (config.offset + config.slope * food.nutrition());
        // Never push an item past its own time just because of the clamp
        double min = Math.min(config.minSeconds, seconds);
        double max = Math.max(config.maxSeconds, seconds);
        scaled = Math.max(min, Math.min(max, scaled));
        return Math.max(1, (int) Math.round(scaled * 20.0));
    }

    /** logs/gameoverse-eat-duration.tsv: every food, its nutrition and its old and new eating time. */
    private static void writeReport() {
        record Row(String id, int nutrition, int baseTicks, int ticks) {
        }
        List<Row> rows = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) {
                continue;
            }
            Consumable consumable = stack.get(DataComponents.CONSUMABLE);
            int base = consumable == null ? -1 : consumable.consumeTicks();
            int ticks;
            try {
                ticks = stack.getUseDuration(null);
            } catch (RuntimeException e) {
                ticks = -1;
            }
            rows.add(new Row(BuiltInRegistries.ITEM.getKey(item).toString(), food.nutrition(), base, ticks));
        }
        rows.sort(Comparator.comparingInt(Row::nutrition).thenComparing(Row::id));

        Path path = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("gameoverse-eat-duration.tsv");
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("item\tnutrition\tbase_seconds\tseconds\n");
            for (Row row : rows) {
                writer.write(String.format(Locale.ROOT, "%s\t%d\t%.2f\t%.2f%n",
                    row.id(), row.nutrition(), row.baseTicks() / 20.0, row.ticks() / 20.0));
            }
        } catch (IOException e) {
            LOGGER.warn("Couldn't write {}", path, e);
            return;
        }
        long changed = rows.stream().filter(r -> r.ticks() != r.baseTicks()).count();
        LOGGER.info("{} foods, {} with a changed eating time; table in {}", rows.size(), changed, path);
    }
}
