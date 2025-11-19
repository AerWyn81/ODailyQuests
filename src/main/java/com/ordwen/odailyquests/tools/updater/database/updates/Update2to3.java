package com.ordwen.odailyquests.tools.updater.database.updates;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.Database;
import com.ordwen.odailyquests.enums.StorageMode;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.updater.database.DatabaseUpdater;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Update2to3 extends DatabaseUpdater {

    public Update2to3(ODailyQuests plugin) {
        super(plugin);
    }

    @Override
    public void apply(ODailyQuests plugin, String version) {
        final StorageMode mode = Database.getMode();
        switch (mode) {
            case MYSQL -> applyMySQL();
            case SQLITE -> applySQLite();
            case YAML -> applyYAML();
        }

        updateVersion(version);
    }

    @Override
    public void applyMySQL() {
        alterProgressionTable("ALTER TABLE `odq_progression` ADD COLUMN `category` VARCHAR(50) NULL AFTER `quest_index`;");
    }

    @Override
    public void applySQLite() {
        alterProgressionTable("ALTER TABLE `odq_progression` ADD COLUMN `category` TEXT;");
    }

    @Override
    public void applyYAML() {
        PluginLogger.info("No YAML database structure changes required for update 2 -> 3.");
    }

    private void alterProgressionTable(String query) {
        if (databaseManager.getSqlManager() == null) {
            PluginLogger.warn("SQL manager not initialized. Skipping database migration 2 -> 3.");
            return;
        }

        try (Connection connection = databaseManager.getSqlManager().getConnection()) {
            if (connection == null) {
                PluginLogger.error("Unable to obtain a database connection for migration 2 -> 3.");
                return;
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute(query);
                PluginLogger.info("Database migration 2 -> 3 applied successfully.");
            }
        } catch (SQLException exception) {
            final String message = exception.getMessage();
            if (message != null && message.toLowerCase().contains("duplicate column")) {
                PluginLogger.info("Column 'category' already exists in odq_progression. Skipping alteration.");
                return;
            }

            PluginLogger.error("Failed to apply database migration 2 -> 3: " + exception.getMessage());
        }
    }
}

