package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Diary {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/digital_diary";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @FXML
    private TextField dateField;

    @FXML
    private TextArea entryTextArea;

    @FXML
    private ComboBox<String> moodComboBox; 

    @FXML
    private ListView<String> entryListView;

    private List<Integer> entryIds = new ArrayList<>();

    private String userNickname; 

    public void setUserNickname(String nickname) {
        this.userNickname = nickname;
    }

    @FXML
    public void initialize() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            createDiaryTable(conn);
            displayDiaryEntries(conn);
            greetUser();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createDiaryTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS diary_entries (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY," +
                     "date DATE," +
                     "entry TEXT," +
                     "mood VARCHAR(20))"; 
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    @FXML
    private void addEntry() {
        String date = dateField.getText();
        String entry = entryTextArea.getText();
        String mood = moodComboBox.getValue(); 
        if (!date.isEmpty() && !entry.isEmpty() && mood != null) {
            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                addDiaryEntry(conn, date, entry, mood);
                displayDiaryEntries(conn);
                dateField.clear();
                entryTextArea.clear();
                moodComboBox.setValue(null); 
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Date, entry, and mood cannot be empty.");
        }
    }

    private void addDiaryEntry(Connection conn, String date, String entry, String mood) throws SQLException {
        String sql = "INSERT INTO diary_entries (date, entry, mood) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            pstmt.setString(2, entry);
            pstmt.setString(3, mood);
            pstmt.executeUpdate();
        }
    }

    @FXML
    private void updateEntry() {
        int selectedIndex = entryListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            int entryId = entryIds.get(selectedIndex);
            String newEntry = entryTextArea.getText();
            String newMood = moodComboBox.getValue(); 
            if (newMood != null) {
                try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                    updateDiaryEntry(conn, entryId, newEntry, newMood);
                    displayDiaryEntries(conn);
                    entryTextArea.clear();
                    moodComboBox.setValue(null); 
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                showAlert("Error", "Please select a mood.");
            }
        } else {
            showAlert("Error", "Please select an entry to update.");
        }
    }

    private void updateDiaryEntry(Connection conn, int entryId, String newEntry, String newMood) throws SQLException {
        String sql = "UPDATE diary_entries SET entry = ?, mood = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newEntry);
            pstmt.setString(2, newMood);
            pstmt.setInt(3, entryId);
            pstmt.executeUpdate();
        }
    }

    @FXML
    private void deleteEntry() {
        int selectedIndex = entryListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            int entryId = entryIds.get(selectedIndex);
            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                deleteDiaryEntry(conn, entryId);
                displayDiaryEntries(conn);
                entryTextArea.clear();
                moodComboBox.setValue(null); 
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Please select an entry to delete.");
        }
    }

    private void deleteDiaryEntry(Connection conn, int entryId) throws SQLException {
        String sql = "DELETE FROM diary_entries WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            pstmt.executeUpdate();
        }
    }

    private void displayDiaryEntries(Connection conn) throws SQLException {
        entryListView.getItems().clear();
        entryIds.clear();
        String sql = "SELECT * FROM diary_entries";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date");
                String entry = rs.getString("entry");
                String mood = rs.getString("mood"); 
                entryListView.getItems().add(date + ": " + entry + " (Mood: " + mood + ")");
                entryIds.add(id);
            }
        }
    }

    private void greetUser() {
        showAlert("Welcome", "Hello, " + userNickname + "!");
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();
            entryTextArea.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
