package com.bettertools.config;

import java.util.HashMap;
import java.util.Map;

public class ToolConfig {
    public boolean threeByThree = false;
    public boolean autoSmelt = false;
    public Map<String, Integer> enchantments = new HashMap<>();

    public ToolConfig() {}

    public ToolConfig copy() {
        ToolConfig copy = new ToolConfig();
        copy.threeByThree = this.threeByThree;
        copy.autoSmelt = this.autoSmelt;
        copy.enchantments = new HashMap<>(this.enchantments);
        return copy;
    }
}
