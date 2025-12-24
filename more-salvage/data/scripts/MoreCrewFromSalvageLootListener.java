package data.scripts;

import java.util.Random;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;

public class MoreCrewFromSalvageLootListener implements ShowLootListener {

    // Tweak these.
    private static final float BASE_EXTRA_CREW_CHANCE = 0.45f;   // 45% chance to inject extra crew
    private static final float DEBRIS_FIELD_BONUS_CHANCE = 0.20f; // +20% if it’s a debris field
    private static final int MIN_EXTRA_CREW = 5;
    private static final int MAX_EXTRA_CREW = 25;

    // Optional: don’t spam crew into tiny “nothing” loot screens
    private static final int MIN_EXISTING_LOOT_STACKS = 1;

    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        if (loot == null || dialog == null) return;

        SectorEntityToken target = dialog.getInteractionTarget();
        if (target == null) return;

        // Filter: only touch *salvage* loot, not mission rewards, bar events, etc.
        // SalvageEntity sets/uses SALVAGE_SEED; debris fields also use SALVAGE_DEBRIS_FIELD.
        if (!target.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SEED)
                && !target.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_DEBRIS_FIELD)) {
            return;
        }

        if (loot.getStacksCopy() == null || loot.getStacksCopy().size() < MIN_EXISTING_LOOT_STACKS) return;

        long seed = 0L;
        if (target.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SEED)) {
            seed = target.getMemoryWithoutUpdate().getLong(MemFlags.SALVAGE_SEED);
        } else {
            // fallback; should be rare
            seed = Misc.getRandomLong(1_000_000_000L);
        }

        // Use a salted deterministic RNG so results are stable-ish per target.
        Random rng = Misc.getRandom(seed, 771234);

        float chance = BASE_EXTRA_CREW_CHANCE;
        if (target.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_DEBRIS_FIELD)) {
            chance += DEBRIS_FIELD_BONUS_CHANCE;
        }
        if (chance > 0.95f) chance = 0.95f;

        if (rng.nextFloat() > chance) return;

        int extra = MIN_EXTRA_CREW + rng.nextInt(Math.max(1, (MAX_EXTRA_CREW - MIN_EXTRA_CREW + 1)));

        // Actually add the crew to the loot pile.
        loot.addCrew(extra);
        loot.sort();
    }
}
