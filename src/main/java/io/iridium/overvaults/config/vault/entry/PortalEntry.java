package io.iridium.overvaults.config.vault.entry;

import com.google.gson.annotations.Expose;

public class PortalEntry {
    @Expose private final CrystalDataEntry crystalData;
    @Expose private final String translationComponent;
    @Expose private final boolean shouldDecay;

    public PortalEntry(CrystalDataEntry crystalData, String translationComponent, boolean shouldDecay) {
        this.crystalData = crystalData;
        this.translationComponent = translationComponent;
        this.shouldDecay = shouldDecay;
    }

    public CrystalDataEntry getCrystalData() {
        return crystalData;
    }

    public String getTranslationComponent() {
        return translationComponent;
    }

    public boolean shouldPortalDecay() {
        return shouldDecay;
    }
}
