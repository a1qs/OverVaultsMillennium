package io.iridium.overvaults.config.vault.entry;

import com.google.gson.annotations.Expose;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import iskallia.vault.item.crystal.time.CrystalTime;

import java.util.List;

public class CrystalDataEntry {

    @Expose private CrystalObjective objective;
    @Expose private CrystalLayout layout;
    @Expose private CrystalTheme theme;
    @Expose private List<ModifierStackEntry> modifiers;
    @Expose private CrystalTime time;

    @Expose private Integer vaultLevel;
    @Expose private Boolean rollRandomModifiers;

    public CrystalDataEntry(CrystalObjective objective, CrystalLayout layout, CrystalTheme theme, List<ModifierStackEntry> modifiers, CrystalTime time, int vaultLevel, boolean rollRandomModifiers) {
        this.objective = objective;
        this.layout = layout;
        this.theme = theme;
        this.modifiers = modifiers;
        this.time = time;
        this.vaultLevel = vaultLevel;
        this.rollRandomModifiers = rollRandomModifiers;
    }

    public CrystalObjective getObjective() {
        return objective;
    }

    public void setObjective(CrystalObjective objective) {
        this.objective = objective;
    }

    public CrystalLayout getLayout() {
        return layout;
    }

    public void setLayout(CrystalLayout layout) {
        this.layout = layout;
    }

    public CrystalTheme getTheme() {
        return theme;
    }

    public void setTheme(CrystalTheme theme) {
        this.theme = theme;
    }

    public CrystalTime getTime() {
        return time;
    }

    public void setTime(CrystalTime time) {
        this.time = time;
    }

    public List<ModifierStackEntry> getModifiers() {
        return modifiers;
    }


    public Integer getVaultLevel() {
        return vaultLevel;
    }

    public void setVaultLevel(Integer vaultLevel) {
        this.vaultLevel = vaultLevel;
    }

    public static CrystalData applyData(CrystalDataEntry entry, CrystalData crystal) {
        if(entry.objective != null) crystal.setObjective(entry.objective);
        if(entry.layout != null) crystal.setLayout(entry.layout);
        if(entry.theme != null) crystal.setTheme(entry.theme);
        if(entry.time != null) crystal.setTime(entry.time);
        if(entry.modifiers != null) crystal.getModifiers().getList().addAll(ModifierStackEntry.getModifiers(entry.modifiers));
        if(entry.vaultLevel != null) crystal.getProperties().setLevel(entry.vaultLevel);
        if(entry.rollRandomModifiers != null) crystal.getModifiers().setRandomModifiers(entry.rollRandomModifiers);

        return crystal;
    }

    public static CrystalData applyData(CrystalDataEntry entry) {
        CrystalData crystal = CrystalData.empty();

        if(entry.objective != null) crystal.setObjective(entry.objective);
        if(entry.layout != null) crystal.setLayout(entry.layout);
        if(entry.theme != null) crystal.setTheme(entry.theme);
        if(entry.time != null) crystal.setTime(entry.time);
        if(entry.modifiers != null) crystal.getModifiers().getList().addAll(ModifierStackEntry.getModifiers(entry.modifiers));
        if(entry.vaultLevel != null) crystal.getProperties().setLevel(entry.vaultLevel);
        if(entry.rollRandomModifiers != null) crystal.getModifiers().setRandomModifiers(entry.rollRandomModifiers);

        return crystal;
    }

}
