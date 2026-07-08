package com.smabedi.hollowknight.controllers.repositories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.models.game.GameSession;

import java.sql.*;

public class DatabaseManager {
    // Stores the database file locally in the user's OS-specific app data folder
    private static final String DB_URL;
    private static final Json json = new Json();

    static {
        json.setOutputType(JsonWriter.OutputType.json);

        // 1. Ensure the saves directory exists before SQLite tries to use it
        FileHandle saveDir = Gdx.files.local(Constants.Paths.Saves.ROOT_PATH);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        // 2. Safely construct the absolute URL now that the folder exists
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

    public static void saveSession(GameSession session) {
        // UPSERT logic: Insert a new row, or update it if the slot_index already exists
        String upsertSQL = "INSERT INTO save_slots (slot_index, session_blob) VALUES (?, ?) "
            + "ON CONFLICT(slot_index) DO UPDATE SET session_blob = excluded.session_blob;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(upsertSQL)) {

            preparedStatement.setInt(1, session.slotIndex);
            preparedStatement.setString(2, json.toJson(session)); // Serialize object to JSON string
            preparedStatement.executeUpdate();
            System.out.println("Successfully saved session to slot " + session.slotIndex);

        } catch (Exception e) {
            System.err.println("Failed to save session: " + e.getMessage());
        }
    }

    public static GameSession loadSession(int slotIndex) {
        String selectSQL = "SELECT session_blob FROM save_slots WHERE slot_index = ?;";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(selectSQL)) {

            preparedStatement.setInt(1, slotIndex);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                String blob = rs.getString("session_blob");
                return json.fromJson(GameSession.class, blob); // Deserialize JSON string back to object
            }
        } catch (Exception e) {
            System.err.println("Failed to load session: " + e.getMessage());
        }
        return null; // Slot is completely empty
    }

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
