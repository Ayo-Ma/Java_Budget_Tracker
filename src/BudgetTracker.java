
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BudgetTracker extends JFrame {

    private final BudgetTableModel tableModel;
    private final JTable table;


    private final JTextField dateField;

    private final JTextField descriptionField;

    private final JTextField amountField;

    private final JComboBox<String> typeComboBox;

    private final JDateChooser dateChooser;


    private final JButton addButton;
    private final JButton editButton;
    private final JButton deleteButton;



    private final JLabel TotalBalance;
    private double balance;

    public BudgetTracker() {


        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
        } catch (Exception e) {
            System.out.println("Not able to set theme");
        }
        // FlatF Theme Customisation
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextField.background", Color.DARK_GRAY);
        UIManager.put("TextField.CaretForeground", Color.RED);
        UIManager.put("ComboBox.foreground", Color.decode("#709dff"));
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", Color.decode("#709dff"));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.background", Color.decode("#709dff"));
        UIManager.put("Label.foreground", Color.WHITE);

        // Adding Styles to element on the window





        Font myFont = new Font("Poppins", Font.PLAIN, 16);
        UIManager.put("Label.font", myFont);
        UIManager.put("TextField.font", myFont);
        UIManager.put("ComboBox.font", myFont);
        UIManager.put("Button.font", myFont);
// Changing Font

        balance = 0.0;
        //Setting Balance to zero
        tableModel = new BudgetTableModel();

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        dateField = new JTextField(10);
        descriptionField = new JTextField(20);
        amountField = new JTextField(10);
        typeComboBox = new JComboBox<>(new String[]{"Expense", "Income", "RecurringCost"});
// Button to Add , Edit and Delete Entries
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");

//Adding Button Styles
        editButton.setForeground(addButton.getForeground());
        editButton.setBackground(addButton.getBackground());
        deleteButton.setForeground(addButton.getForeground());
        deleteButton.setBackground(addButton.getBackground());
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");

        TotalBalance = new JLabel("Balance: $" + balance);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Date"));
        inputPanel.add(dateChooser);




        inputPanel.add(new JLabel("Description"));
        inputPanel.add(descriptionField);

        inputPanel.add(new JLabel("Amount"));
        inputPanel.add(amountField);

        inputPanel.add(new JLabel("Category"));
        inputPanel.add(typeComboBox);


        inputPanel.add(addButton);
        inputPanel.add(editButton);



        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(TotalBalance);
        bottomPanel.add(deleteButton);
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);

        add(scrollPane, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.WEST);

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(BudgetTracker.this, "Please select an entry to edit", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Retrieve the selected entry
            AddNewEntry selectedEntry = tableModel.getEntryAt(selectedRow);

            // Show the edit modal
            showEditModal(selectedEntry, selectedRow);
        });


        // Add ActionListener for the delete button
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Retrieve the selected row index
                int selectedRow = table.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(BudgetTracker.this, "Please select an entry to delete", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get values from the selected row
                String date = (String) table.getValueAt(selectedRow, 0);
                String description = (String) table.getValueAt(selectedRow, 1);

                // Confirm deletion
                int confirm = JOptionPane.showConfirmDialog(
                        BudgetTracker.this,
                        "Are you sure you want to delete this entry?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    deleteEntryFromDatabase(date, description);

                    // Removing  entry from the table model
                    tableModel.removeEntry(selectedRow);
                    updateTotalBalance();


                    JOptionPane.showMessageDialog(BudgetTracker.this, "Entry deleted successfully!");
                }
            }
        });




        loadDataFromDatabase();


//      Listening for Button Events
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                Method to save entries into the database
                saveToDatabase();
            }
        });

//        hey

        // Panel for date range query
        JPanel rangePanel = new JPanel();
        rangePanel.add(new JLabel("Start Date:"));
        JDateChooser startDateChooser = new JDateChooser();
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        rangePanel.add(startDateChooser);

        rangePanel.add(new JLabel("End Date:"));
        JDateChooser endDateChooser = new JDateChooser();
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        rangePanel.add(endDateChooser);

        JButton rangeQueryButton = new JButton("Show Entries and Totals");
        rangePanel.add(rangeQueryButton);

// Add rangePanel to the frame (at the bottom)
        add(rangePanel, BorderLayout.SOUTH);

// Add ActionListener for the button
        rangeQueryButton.addActionListener(e -> showEntriesAndTotalsForRange(startDateChooser, endDateChooser));



//       Stop


//Setting Window Title
        setTitle("Budget Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

    }


    private void showEntriesAndTotalsForRange(JDateChooser startDateChooser, JDateChooser endDateChooser) {
        // Get the selected dates
        Date startDate = startDateChooser.getDate();
        Date endDate = endDateChooser.getDate();

        if (startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(this, "Please select both start and end dates", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (endDate.before(startDate)) {
            JOptionPane.showMessageDialog(this, "End date must be after start date", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String start = dateFormat.format(startDate);
        String end = dateFormat.format(endDate);

        double totalIncome = 0.0;
        double totalExpense = 0.0;

        // temporary table model to hold the filtered entries
        BudgetTableModel filteredTableModel = new BudgetTableModel();

        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "SELECT date, description, amount, category FROM budget_entries WHERE date BETWEEN ? AND ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, start);
            statement.setString(2, end);

            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String date = resultSet.getString("date");
                String description = resultSet.getString("description");
                double amount = resultSet.getDouble("amount");
                String category = resultSet.getString("category");

                // Add to filtered table model
                AddNewEntry entry = new AddNewEntry(date, description, amount, category);
                filteredTableModel.addEntry(entry);

                // Calculate totals
                if ("Income".equalsIgnoreCase(category)) {
                    totalIncome += amount;
                } else if ("Expense".equalsIgnoreCase(category) || "RecurringCost".equalsIgnoreCase(category)) {
                    totalExpense += amount;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // results in a new modal
        showResultsInModal(filteredTableModel, totalIncome, totalExpense);
    }

    private void showResultsInModal(BudgetTableModel filteredTableModel, double totalIncome, double totalExpense) {
        JDialog resultsDialog = new JDialog(this, "Date Range Results", true);
        resultsDialog.setLayout(new BorderLayout());
        resultsDialog.setSize(600, 400);
        resultsDialog.setLocationRelativeTo(this);

        // Table to show detailed entries
        JTable resultsTable = new JTable(filteredTableModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        resultsDialog.add(scrollPane, BorderLayout.CENTER);

        // Panel for totals and export button
        JPanel totalsPanel = new JPanel(new BorderLayout());
        JPanel totalsInfoPanel = new JPanel(new GridLayout(2, 1));
        JLabel incomeLabel = new JLabel("Total Income: $" + totalIncome);
        JLabel expenseLabel = new JLabel("Total Expenses: $" + totalExpense);
        incomeLabel.setFont(new Font("Poppins", Font.BOLD, 16));
        expenseLabel.setFont(new Font("Poppins", Font.BOLD, 16));

        totalsInfoPanel.add(incomeLabel);
        totalsInfoPanel.add(expenseLabel);

        JButton exportButton = new JButton("Export to File");
        exportButton.addActionListener(e -> exportResultsToFile(filteredTableModel, totalIncome, totalExpense));

        totalsPanel.add(totalsInfoPanel, BorderLayout.CENTER);
        totalsPanel.add(exportButton, BorderLayout.EAST);

        resultsDialog.add(totalsPanel, BorderLayout.SOUTH);

        resultsDialog.setVisible(true);
    }

    private void exportResultsToFile(BudgetTableModel tableModel, double totalIncome, double totalExpense) {
        // Let the user choose a file to save
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Results to File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            // Get the selected file path
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();

            // Ensuring the file has a .csv extension
            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }

            try (PrintWriter writer = new PrintWriter(filePath)) {
                // Write header
                writer.println("Date,Description,Amount,Category");

                // Write entries
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String date = (String) tableModel.getValueAt(i, 0);
                    String description = (String) tableModel.getValueAt(i, 1);
                    double amount = (double) tableModel.getValueAt(i, 2);
                    String category = (String) tableModel.getValueAt(i, 3);

                    writer.printf("%s,%s,%.2f,%s%n", date, description, amount, category);
                }

                // Write totals
                writer.println();
                writer.printf("Total Income($),%.2f%n", totalIncome);
                writer.printf("Total Expenses($),%.2f%n", totalExpense);

                // Notify the user
                JOptionPane.showMessageDialog(this, "Results exported successfully to: " + filePath, "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting results: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Modal Popup for editing entries
    private void showEditModal(AddNewEntry entry, int rowIndex) {
        JDialog editDialog = new JDialog(this, "Edit Entry", true); // Modal dialog
        editDialog.setLayout(new GridLayout(5, 2, 10, 10));
        editDialog.setSize(400, 300);
        editDialog.setLocationRelativeTo(this);

        // Fields to edit the entry
        JLabel dateLabel = new JLabel("Date:");
        JDateChooser modalDateChooser = new JDateChooser();
        modalDateChooser.setDateFormatString("yyyy-MM-dd");

        JLabel descriptionLabel = new JLabel("Description:");
        JTextField modalDescriptionField = new JTextField(entry.getDescription());

        JLabel amountLabel = new JLabel("Amount:");
        JTextField modalAmountField = new JTextField(String.valueOf(entry.getAmount()));

        JLabel typeLabel = new JLabel("Category:");
        JComboBox<String> modalTypeComboBox = new JComboBox<>(new String[]{"Expense", "Income", "RecurringCost"});
        modalTypeComboBox.setSelectedItem(entry.getType());

        // Populating  fields with current entry data
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            modalDateChooser.setDate(dateFormat.parse(entry.getDate()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format in entry", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Buttons: Sync Changes and Cancel
        JButton syncButton = new JButton("Sync Changes");
        JButton cancelButton = new JButton("Cancel");

        // Add components to the dialog
        editDialog.add(dateLabel);
        editDialog.add(modalDateChooser);
        editDialog.add(descriptionLabel);
        editDialog.add(modalDescriptionField);
        editDialog.add(amountLabel);
        editDialog.add(modalAmountField);
        editDialog.add(typeLabel);
        editDialog.add(modalTypeComboBox);
        editDialog.add(syncButton);
        editDialog.add(cancelButton);

        // Sync Changes action
        syncButton.addActionListener(e -> {
            // Validate selected date
            Date selectedDate = modalDateChooser.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(editDialog, "Please select a valid date", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Format the selected date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String updatedDate = dateFormat.format(selectedDate);

            // Validate other inputs
            String updatedDescription = modalDescriptionField.getText();
            String amountText = modalAmountField.getText();
            String updatedCategory = (String) modalTypeComboBox.getSelectedItem();

            if (updatedDescription.isEmpty() || amountText.isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "All fields must be filled out", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double updatedAmount;
            try {
                updatedAmount = Double.parseDouble(amountText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(editDialog, "Invalid amount format", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Update entry in the database
                try (Connection connection = DatabaseManager.getConnection()) {
                    String sql = "UPDATE budget_entries SET date = ?, description = ?, amount = ?, category = ? WHERE date = ? AND description = ?";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, updatedDate);
                    statement.setString(2, updatedDescription);
                    statement.setDouble(3, updatedAmount);
                    statement.setString(4, updatedCategory);
                    statement.setString(5, entry.getDate());
                    statement.setString(6, entry.getDescription());

                    int rowsUpdated = statement.executeUpdate();
                    if (rowsUpdated > 0) {
                        // Update table model
                        AddNewEntry updatedEntry = new AddNewEntry(updatedDate, updatedDescription, updatedAmount, updatedCategory);
                        tableModel.updateEntry(rowIndex, updatedEntry);
                        updateTotalBalance();


                        JOptionPane.showMessageDialog(editDialog, "Entry updated successfully!");
                        editDialog.dispose(); // Close the modal
                    } else {
                        JOptionPane.showMessageDialog(editDialog, "No matching entry found to update", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(editDialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cancel button action
        cancelButton.addActionListener(e -> editDialog.dispose());

        editDialog.setVisible(true);
    }



// Delete Entry Fro Database
    private void deleteEntryFromDatabase(String date, String description) {
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM budget_entries WHERE date = ? AND description = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, date);
            statement.setString(2, description);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted == 0) {
                JOptionPane.showMessageDialog(this, "No entry found to delete", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

//    Update Total Balance Based on either Category
    private void updateTotalBalance() {
        balance = tableModel.calculateTotalBalance();
        TotalBalance.setText("Balance: $" + balance);
    }


//Add Entries to Database
    private void saveToDatabase() {
        // Get the selected date from the JDateChooser
        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a date", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Format the selected date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(selectedDate);

        // Other field values
        String description = descriptionField.getText();
        String amountText = amountField.getText();
        String category = (String) typeComboBox.getSelectedItem();

        // Validate other inputs
        if (description.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            // Save to the database
            try (Connection connection = DatabaseManager.getConnection()) {
                String sql = "INSERT INTO budget_entries (date, description, amount, category) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, date);
                statement.setString(2, description);
                statement.setDouble(3, amount);
                statement.setString(4, category);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Data saved successfully!");

                    // Add the new entry to the table model
                    AddNewEntry newEntry = new AddNewEntry(date, description, amount, category);
                    tableModel.addEntry(newEntry);
                    updateTotalBalance();


                    // Clear fields after saving
                    dateChooser.setDate(null); // Clear the date chooser
                    descriptionField.setText("");
                    amountField.setText("");
                    typeComboBox.setSelectedIndex(0);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount format", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

// Make Data Available when Windows Open or Close
    private void loadDataFromDatabase() {
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "SELECT date, description, amount, category FROM budget_entries";
            PreparedStatement statement = connection.prepareStatement(sql);

            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String date = resultSet.getString("date");
                String description = resultSet.getString("description");
                double amount = resultSet.getDouble("amount");
                String category = resultSet.getString("category");

                // Create an AddNewEntry object and add it to the table model
                AddNewEntry entry = new AddNewEntry(date, description, amount, category);
                tableModel.addEntry(entry);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }


        updateTotalBalance();

    }


}
