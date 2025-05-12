package com.yourcompany.schedule.ui;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import com.yourcompany.schedule.data.DataManager;
import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.model.ScheduleEntry;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import com.yourcompany.schedule.core.Scheduler;
import com.yourcompany.schedule.core.ConflictChecker;

public class MainScheduleFrame extends JFrame {
    private SchedulePanel schedulePanel;
    private DataManager dataManager;
    private List<Course> courses;
    private List<Room> rooms;
    private List<ScheduleEntry> scheduleEntries;
    private Scheduler scheduler;
    private ConflictChecker conflictChecker;
    private TimetablePanel timetablePanel;
    private JLabel weekLabel;
    private DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MainScheduleFrame() {
        setTitle("Schedule Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        timetablePanel = new TimetablePanel();
        schedulePanel = new SchedulePanel();
        
        // Create week navigation panel
        JPanel weekNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevWeekButton = new JButton("◀ Previous Week");
        JButton currentWeekButton = new JButton("Current Week");
        JButton nextWeekButton = new JButton("Next Week ▶");
        weekLabel = new JLabel();
        updateWeekLabel();
        
        weekLabel.setFont(weekLabel.getFont().deriveFont(Font.BOLD));
        weekNavPanel.add(prevWeekButton);
        weekNavPanel.add(currentWeekButton);
        weekNavPanel.add(weekLabel);
        weekNavPanel.add(nextWeekButton);
        
        prevWeekButton.addActionListener(e -> {
            timetablePanel.previousWeek();
            updateWeekLabel();
            refreshTable();
        });
        
        currentWeekButton.addActionListener(e -> {
            timetablePanel.setCurrentWeek(java.time.LocalDate.now());
            updateWeekLabel();
            refreshTable();
        });
        
        nextWeekButton.addActionListener(e -> {
            timetablePanel.nextWeek();
            updateWeekLabel();
            refreshTable();
        });
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Create a scroll pane for the timetable panel
        JScrollPane timetableScrollPane = new JScrollPane(timetablePanel);
        timetableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        timetableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        timetableScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Make scrolling smoother
        
        // Add week navigation to the top of timetable tab
        JPanel timetablePanel = new JPanel(new BorderLayout());
        timetablePanel.add(weekNavPanel, BorderLayout.NORTH);
        timetablePanel.add(timetableScrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("Timetable", timetablePanel);
        tabbedPane.addTab("Schedule Table", schedulePanel);
        add(tabbedPane, BorderLayout.CENTER);
        // Add button panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton manageCoursesButton = new JButton("Manage Courses");
        JButton manageRoomsButton = new JButton("Manage Rooms");
        JButton manageTeachersButton = new JButton("Manage Teachers");
        JButton manageClassesButton = new JButton("Manage Classes");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(manageCoursesButton);
        buttonPanel.add(manageRoomsButton);
        buttonPanel.add(manageTeachersButton);
        buttonPanel.add(manageClassesButton);
        add(buttonPanel, BorderLayout.SOUTH);
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        // Load data from database
        loadData();
        // Button actions (to be implemented)
        scheduler = new Scheduler();
        conflictChecker = new ConflictChecker();
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AddEditEntryDialog dialog = new AddEditEntryDialog(MainScheduleFrame.this, courses, rooms, null);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    ScheduleEntry newEntry = dialog.getEntry();
                    if (newEntry == null) return;
                    // Check for conflicts
                    if (!scheduler.canAddEntry(newEntry, scheduleEntries)) {
                        new ConflictDialog(MainScheduleFrame.this, "Schedule conflict detected!").setVisible(true);
                        return;
                    }
                    try {
                        dataManager.addScheduleEntry(newEntry);
                        refreshTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainScheduleFrame.this, "Error adding entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = schedulePanel.getSelectedRow();
                if (selectedRow < 0 || selectedRow >= scheduleEntries.size()) {
                    JOptionPane.showMessageDialog(MainScheduleFrame.this, "Please select an entry to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                ScheduleEntry selectedEntry = scheduleEntries.get(selectedRow);
                AddEditEntryDialog dialog = new AddEditEntryDialog(MainScheduleFrame.this, courses, rooms, selectedEntry);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    ScheduleEntry updatedEntry = dialog.getEntry();
                    if (updatedEntry == null) return;
                    updatedEntry.setEntryId(selectedEntry.getEntryId());
                    // Remove the current entry from the list for conflict check
                    List<ScheduleEntry> tempList = new java.util.ArrayList<>(scheduleEntries);
                    tempList.remove(selectedEntry);
                    if (!scheduler.canAddEntry(updatedEntry, tempList)) {
                        new ConflictDialog(MainScheduleFrame.this, "Schedule conflict detected!").setVisible(true);
                        return;
                    }
                    try {
                        dataManager.updateScheduleEntry(updatedEntry);
                        refreshTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainScheduleFrame.this, "Error updating entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = schedulePanel.getSelectedRow();
                if (selectedRow < 0 || selectedRow >= scheduleEntries.size()) {
                    JOptionPane.showMessageDialog(MainScheduleFrame.this, "Please select an entry to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(MainScheduleFrame.this, "Are you sure you want to delete this entry?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        dataManager.deleteScheduleEntry(scheduleEntries.get(selectedRow).getEntryId());
                        refreshTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainScheduleFrame.this, "Error deleting entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        manageCoursesButton.addActionListener(e -> {
            ManageCoursesDialog dialog = new ManageCoursesDialog(MainScheduleFrame.this, dataManager);
            dialog.setVisible(true);
            refreshTable();
        });
        manageRoomsButton.addActionListener(e -> {
            ManageRoomsDialog dialog = new ManageRoomsDialog(MainScheduleFrame.this, dataManager);
            dialog.setVisible(true);
            refreshTable();
        });
        manageTeachersButton.addActionListener(e -> {
            ManageTeachersDialog dialog = new ManageTeachersDialog(MainScheduleFrame.this, dataManager);
            dialog.setVisible(true);
            refreshTable();
        });
        manageClassesButton.addActionListener(e -> {
            ManageClassesDialog dialog = new ManageClassesDialog(MainScheduleFrame.this, dataManager);
            dialog.setVisible(true);
            refreshTable();
        });
    }

    private void loadData() {
        try {
            dataManager = new DataManager();
            
            // Initialize the database schema and seed data if needed
            try {
                dataManager.initializeDatabase();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error initializing database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            courses = dataManager.getAllCourses();
            rooms = dataManager.getAllRooms();
            scheduleEntries = dataManager.getAllScheduleEntries(courses, rooms);
            timetablePanel.setEntries(scheduleEntries);
            schedulePanel.clearTable();
            for (ScheduleEntry entry : scheduleEntries) {
                schedulePanel.addScheduleRow(entry);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        try {
            courses = dataManager.getAllCourses();
            rooms = dataManager.getAllRooms();
            scheduleEntries = dataManager.getAllScheduleEntries(courses, rooms);
            
            // Filter entries for the timetable based on the selected week
            List<ScheduleEntry> weekEntries = filterEntriesForSelectedWeek(scheduleEntries);
            timetablePanel.setEntries(weekEntries);
            
            // Update the schedule table with all entries
            schedulePanel.clearTable();
            for (ScheduleEntry entry : scheduleEntries) {
                schedulePanel.addScheduleRow(entry);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Filters schedule entries to only include those in the currently selected week
     */
    private List<ScheduleEntry> filterEntriesForSelectedWeek(List<ScheduleEntry> allEntries) {
        if (allEntries == null) return new java.util.ArrayList<>();
        
        java.time.LocalDate weekStart = timetablePanel.getCurrentWeekStart();
        java.time.LocalDate weekEnd = weekStart.plusDays(6);
        
        return allEntries.stream()
            .filter(entry -> {
                if (entry.getStartDateTime() == null) return false;
                
                java.time.LocalDate entryDate = entry.getStartDateTime().toLocalDate();
                return !entryDate.isBefore(weekStart) && !entryDate.isAfter(weekEnd);
            })
            .collect(java.util.stream.Collectors.toList());
    }

    private void updateWeekLabel() {
        String startDate = timetablePanel.getCurrentWeekStart().format(weekFormatter);
        String endDate = timetablePanel.getCurrentWeekStart().plusDays(6).format(weekFormatter);
        int weekNumber = timetablePanel.getCurrentWeekNumber();
        int year = timetablePanel.getCurrentYear();
        weekLabel.setText(String.format("Week %d, %d: %s - %s", weekNumber, year, startDate, endDate));
        
        // Sync the week selection with the schedule panel
        schedulePanel.setCurrentWeek(timetablePanel.getCurrentWeekStart());
    }

    // Add method to handle week changes from schedule panel
    public void setCurrentWeek(java.time.LocalDate date) {
        timetablePanel.setCurrentWeek(date);
        updateWeekLabel();
        refreshTable();
    }
} 