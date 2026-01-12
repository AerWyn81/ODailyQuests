package com.ordwen.odailyquests.configuration.essentials;

import com.ordwen.odailyquests.configuration.ConfigFactory;
import com.ordwen.odailyquests.configuration.IConfigurable;
import com.ordwen.odailyquests.files.implementations.ConfigurationFile;

public class RerollMaximum implements IConfigurable {

    private final ConfigurationFile configurationFile;
    private int rerollMaximumConf;

    public RerollMaximum(ConfigurationFile configurationFile) {
        this.configurationFile = configurationFile;
    }

    @Override
    public void load() {
        final String path = "reroll_maximum";
        rerollMaximumConf = configurationFile.getConfig().getInt(path);
    }

    private static RerollMaximum getInstance() {
        return ConfigFactory.getConfig(RerollMaximum.class);
    }

    public static int getMaxRerolls() {
        return getInstance().rerollMaximumConf;
    }
}
