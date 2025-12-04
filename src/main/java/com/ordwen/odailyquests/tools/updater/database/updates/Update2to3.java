package com.ordwen.odailyquests.tools.updater.database.updates;

import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.configuration.essentials.Database;
import com.ordwen.odailyquests.configuration.essentials.Debugger;
import com.ordwen.odailyquests.enums.StorageMode;
import com.ordwen.odailyquests.files.implementations.ProgressionFile;
import com.ordwen.odailyquests.tools.PluginLogger;
import com.ordwen.odailyquests.tools.updater.database.DatabaseUpdater;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.file.FileConfiguration;

public class Update2to3 extends DatabaseUpdater {

    private static final String MYSQL_ADD_RECENT_REROLLS_COLUMN = """
            ALTER TABLE `odq_player`
            ADD COLUMN `recent_rerolls` INT NOT NULL DEFAULT 0;
            """;

    private static final String SQLITE_ADD_RECENT_REROLLS_COLUMN = """
            ALTER TABLE `odq_player`
            ADD COLUMN `recent_rerolls` INTEGER NOT NULL DEFAULT 0;
            """;

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
        Debugger.write("Applying MySQL database update 2 -> 3: adding recent_rerolls");
        runSqlAlter(MYSQL_ADD_RECENT_REROLLS_COLUMN);
    }

    @Override
    public void applySQLite() {
        alterProgressionTable("ALTER TABLE `odq_progression` ADD COLUMN `category` TEXT;");
        Debugger.write("Applying SQLite database update 2 -> 3: adding recent_rerolls");
        runSqlAlter(SQLITE_ADD_RECENT_REROLLS_COLUMN);
    }

    @Override
    public void applyYAML() {
        PluginLogger.info("No YAML database structure changes required for update 2 -> 3.");

        Debugger.write("Applying YAML database update 2 -> 3: adding recentRerolls");

        final ProgressionFile progression = this.progressionFile;
        final FileConfiguration progressionConfig = progression.getConfig();

        for (String playerKey : progressionConfig.getKeys(false)) {
            final String path = playerKey + ".recentRerolls";

            if (!progressionConfig.isSet(path)) {
                progressionConfig.set(path, 0);
                Debugger.write("Set recentRerolls = 0 for player entry " + playerKey);
            }
        }

        try {
            progressionConfig.save(progression.getFile());
            PluginLogger.warn("YAML database update 2 -> 3 completed!");
        } catch (IOException e) {
            PluginLogger.error("An error occurred while saving YAML data during database update 2 -> 3:");
            PluginLogger.error(e.getMessage());
        }
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

    private void runSqlAlter(String sql) {
        try (Connection connection = databaseManager.getSqlManager().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();
            PluginLogger.warn("SQL database update 2 -> 3 completed!");
        } catch (SQLException e) {
            final String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate column") || msg.contains("already exists")) {
                Debugger.write("recent_rerolls column already exists in odq_player -- skipping!");
            } else {
                PluginLogger.error("Error while applying SQL database update 2 -> 3!");
                PluginLogger.error(e.getMessage());
                Debugger.write("Error while applying SQL database update 2 -> 3!");
                Debugger.write(e.getMessage());
            }
        }
    }
}
