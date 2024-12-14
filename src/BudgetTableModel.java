import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class BudgetTableModel extends AbstractTableModel {

    private final List<AddNewEntry> entries;
    private final String[] columnNames = {"Date","Description","Amount","Type"};

    public BudgetTableModel(){
        entries = new ArrayList<>();
    }

    // Add a new entry to the table model
    public void addEntry(AddNewEntry entry){
        entries.add(entry);
        fireTableRowsInserted(entries.size()-1, entries.size()-1);
    }

    // Remove an entry from the table model
    public void removeEntry(int rowIndex) {
        entries.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    // Update an existing entry in the table model
    public void updateEntry(int rowIndex, AddNewEntry updatedEntry) {
        entries.set(rowIndex, updatedEntry);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column){
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AddNewEntry entry = entries.get(rowIndex);
        switch(columnIndex){
            case 0:
                return entry.getDate();
            case 1:
                return entry.getDescription();
            case 2:
                return entry.getAmount();
            case 3:
                return entry.getType();

            default:
                return null;
        }
    }

   // retrieval of entries for validation
    public AddNewEntry getEntryAt(int rowIndex) {
        return entries.get(rowIndex);
    }



    public double calculateTotalBalance() {
        double balance = 0.0;
        for (AddNewEntry entry : entries) {
            if ("Income".equalsIgnoreCase(entry.getType())) {
                balance += entry.getAmount();
            } else if ("Expense".equalsIgnoreCase(entry.getType()) || "RecurringCost".equalsIgnoreCase(entry.getType())) {
                balance -= entry.getAmount();
            }
        }
        return balance;
    }



}
