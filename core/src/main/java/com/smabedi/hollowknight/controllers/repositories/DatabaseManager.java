package com.smabedi.hollowknight.controllers.repositories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.game.GameSession;

import java.sql.*;

/**
 * Manages the SQLite database for local game session persistence.
 * Utilizes JSON serialization to store complex state objects efficiently within the database.
 */
public class DatabaseManager {
    private static final String DB_URL;
    private static final Json json = new Json();

    static {
        json.setOutputType(JsonWriter.OutputType.json);

        FileHandle saveDir = Gdx.files.local(Constants.Paths.Saves.ROOT_PATH);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        DB_URL = "jdbc:sqlite:" + Gdx.files.local(Constants.Paths.Saves.DATABASE).file().getAbsolutePath();

        json.setOutputType(JsonWriter.OutputType.json);
        try {
            Class.forName("org.sqlite.JDBC");
            initDatabase();
        } catch (Exception e) {
            System.err.println("Critical Error: SQLite JDBC driver not found.");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void initDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS save_slots ("
            + "slot_index INTEGER PRIMARY KEY,"
            + "session_blob TEXT NOT NULL"
            + ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    /**
     * Saves or updates a game session using UPSERT logic.
     */
    public static void saveSession(GameSession session) {
        String upsertSQL = "INSERT INTO save_slots (slot_index, session_blob) VALUES (?, ?) "
            + "ON CONFLICT(slot_index) DO UPDATE SET session_blob = excluded.session_blob;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(upsertSQL)) {

            preparedStatement.setInt(1, session.slotIndex);
            preparedStatement.setString(2, json.toJson(session));
            preparedStatement.executeUpdate();
            System.out.println("Successfully saved session to slot " + session.slotIndex);

        } catch (Exception e) {
            System.err.println("Failed to save session: " + e.getMessage());
        }
    }

    /**
     * Retrieves and deserializes a game session from the database.
     * Also validates specific constraints (e.g., One Shot challenge invalidation).
     */
    public static GameSession loadSession(int slotIndex) {
        String selectSQL = "SELECT session_blob FROM save_slots WHERE slot_index = ?;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(selectSQL)) {

            preparedStatement.setInt(1, slotIndex);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                String blob = rs.getString("session_blob");
                GameSession session = json.fromJson(GameSession.class, blob);

                // The "One Shot" achievement is invalidated upon reloading a save file
                if (session != null) {
                    session.isOneSitting = false;
                }

                return session;
            }
        } catch (Exception e) {
            System.err.println("Failed to load session: " + e.getMessage());
        }
        return null;
    }

    /**
     * Purges a specific save slot from the database.
     */
    public static void deleteSession(int slotIndex) {
        String deleteSQL = "DELETE FROM save_slots WHERE slot_index = ?;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(deleteSQL)) {

            preparedStatement.setInt(1, slotIndex);
            preparedStatement.executeUpdate();
            System.out.println("Cleared session at slot " + slotIndex);

        } catch (Exception e) {
            System.err.println("Failed to delete session: " + e.getMessage());
        }
    }
}
