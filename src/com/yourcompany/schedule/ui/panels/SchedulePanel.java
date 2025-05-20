package com.yourcompany.schedule.ui.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.yourcompany.schedule.model.ScheduleEntry;

public class SchedulePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField filterField;
    private JComboBox<String> filterColumnComboBox;
    private JCheckBox weekFilterCheckBox;
    private LocalDate currentWeekStart;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private List<ScheduleEntry> allEntries = new ArrayList<>();

    public SchedulePanel() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new Object[]{"Course", "Teacher", "Class", "Room", "Start Date/Time", "End Date/Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Add sorting capability
        sorter = new TableRowSorter<>(tableModel);
        
        table = new JTable(tableModel);
        table.setRowSorter(sorter);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Add a header tooltip to explain sorting
        table.getTableHeader().setToolTipText("Click to sort; Shift-click to sort by multiple columns");
        
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Set preferred column widths
        int[] widths = {150, 150, 100, 100, 150, 150};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        
        // Add filter panel
        JPanel filterPanel = new JPanel(new BorderLayout());
        JPanel filterControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        // Add week filter checkbox
        weekFilterCheckBox = new JCheckBox("Current Week Only");
        weekFilterCheckBox.addActionListener(e -> applyFilter());
        filterControlsPanel.add(weekFilterCheckBox);
        
        // Add separator
        filterControlsPanel.add(new JSeparator(JSeparator.VERTICAL) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1, 20);
            }
        });
        
        filterControlsPanel.add(new JLabel("Filter by:"));
        
        // Create column filter dropdown
        filterColumnComboBox = new JComboBox<>(new String[]{"All Columns", "Course", "Teacher", "Class", "Room", "Date"});
        filterControlsPanel.add(filterColumnComboBox);
        
        filterControlsPanel.add(new JLabel("Search:"));
        filterField = new JTextField(20);
        filterControlsPanel.add(filterField);
        
        JButton clearFilterButton = new JButton("Clear");
        clearFilterButton.setToolTipText("Clear all filters");
        filterControlsPanel.add(clearFilterButton);
        
        filterPanel.add(filterControlsPanel, BorderLayout.CENTER);
        
        // Add a border with title
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        
        add(filterPanel, BorderLayout.NORTH);
        
        // Add filter listener
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        
        // Add column filter listener
        filterColumnComboBox.addActionListener(e -> applyFilter());
        
        // Add clear button listener
        clearFilterButton.addActionListener(e -> {
            filterField.setText("");
            filterColumnComboBox.setSelectedIndex(0);
            weekFilterCheckBox.setSelected(false);
            applyFilter();
        });
        
        // Initialize with current week
        setCurrentWeek(LocalDate.now());
    }
    
    public void setCurrentWeek(LocalDate date) {
        // Find the Monday of the week containing the given date
        this.currentWeekStart = date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        applyFilter();
    }
    
    private void applyFilter() {
        String text = filterField.getText().trim().toLowerCase();
        int columnIndex = filterColumnComboBox.getSelectedIndex() - 1; // -1 because "All Columns" is index 0
        boolean weekFilterEnabled = weekFilterCheckBox.isSelected();
        
        if (text.isEmpty() && !weekFilterEnabled) {
            sorter.setRowFilter(null);
            return;
        }
        
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        
        // Text filter
        if (!text.isEmpty()) {
            if (columnIndex < 0) { // "All Columns"
                List<RowFilter<DefaultTableModel, Integer>> textFilters = new ArrayList<>();
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    textFilters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(text), i));
                }
                filters.add(RowFilter.orFilter(textFilters));
            } else {
                filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(text), columnIndex));
            }
        }
        
        // Week filter
        if (weekFilterEnabled) {
            filters.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    int row = entry.getIdentifier();
                    if (row >= allEntries.size()) return false;
                    
                    ScheduleEntry scheduleEntry = allEntries.get(row);
                    if (scheduleEntry.getStartDateTime() == null) return false;
                    
                    LocalDate entryDate = scheduleEntry.getStartDateTime().toLocalDate();
                    LocalDate weekEnd = currentWeekStart.plusDays(6);
                    
                    return !entryDate.isBefore(currentWeekStart) && !entryDate.isAfter(weekEnd);
                }
            });
        }
        
        // Apply filters
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else if (filters.size() == 1) {
            sorter.setRowFilter(filters.get(0));
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    public void addScheduleRow(ScheduleEntry entry) {
        allEntries.add(entry);
        // Extract data from CourseOffering
        com.yourcompany.schedule.model.CourseOffering off = entry.getOffering();
        String courseName = off != null && off.getCourse() != null ? off.getCourse().getCourseName() : "";
        String teacherName = off != null && off.getTeacher() != null ? off.getTeacher().getName() : "";
        String className = off != null && off.getSchoolClass() != null ? off.getSchoolClass().getName() : "";
        tableModel.addRow(new Object[] {
            courseName,
            teacherName,
            className,
            entry.getRoom() != null ? entry.getRoom().getRoomName() : "",
            entry.getStartDateTime() != null ? dtf.format(entry.getStartDateTime()) : "",
            entry.getEndDateTime() != null ? dtf.format(entry.getEndDateTime()) : ""
        });
    }

    public int getSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return -1;
        }
        
        // Convert view row to model row
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow;
    }

    public ScheduleEntry getSelectedEntry() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        
        // Convert view row to model row
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < allEntries.size()) {
            return allEntries.get(modelRow);
        }
        return null;
    }

    public void clearTable() {
        allEntries.clear();
        tableModel.setRowCount(0);
    }
}