package com.yourcompany.schedule.ui.frames;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import com.yourcompany.schedule.data.DataManager;
import com.yourcompany.schedule.model.CourseOffering;
import com.yourcompany.schedule.model.Room;
import com.yourcompany.schedule.model.ScheduleEntry;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import com.yourcompany.schedule.core.Scheduler;
import com.yourcompany.schedule.model.Teacher;
import com.yourcompany.schedule.model.SchoolClass;
import java.sql.SQLException;
import java.util.ArrayList;
import com.yourcompany.schedule.ui.panels.TimetablePanel;
import com.yourcompany.schedule.ui.dialogs.AddEditEntryDialog;
import com.yourcompany.schedule.ui.dialogs.ManageCoursesDialog;
import com.yourcompany.schedule.ui.dialogs.ManageRoomsDialog;
import com.yourcompany.schedule.ui.dialogs.ManageTeachersDialog;
import com.yourcompany.schedule.ui.dialogs.ManageClassesDialog;

public class MainScheduleFrame extends JFrame {
    private DataManager dataManager;
    private Scheduler scheduler;
    private TimetablePanel timetablePanel;
    private JTabbedPane tabbedPane;
    private JPanel schedulePanel;
    private JPanel coursePanel;
    private JPanel roomPanel;
    private JPanel teacherPanel;
    private JPanel classPanel;
    private List<CourseOffering> courseOfferings;
    private List<Room> rooms;
    private List<Teacher> teachers;
    private List<SchoolClass> classes;
    private List<ScheduleEntry> scheduleEntries;

    public MainScheduleFrame() {
        dataManager = new DataManager();
        scheduler = new Scheduler();
        courseOfferings = new ArrayList<>();
        rooms = new ArrayList<>();
        teachers = new ArrayList<>();
        classes = new ArrayList<>();
        scheduleEntries = new ArrayList<>();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setTitle("Schedule Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        timetablePanel = new TimetablePanel();
        schedulePanel = createSchedulePanel();
        coursePanel = createCoursePanel();
        roomPanel = createRoomPanel();
        teacherPanel = createTeacherPanel();
        classPanel = createClassPanel();

        tabbedPane.addTab("Timetable", timetablePanel);
        tabbedPane.addTab("Schedule", schedulePanel);
        tabbedPane.addTab("Courses", coursePanel);
        tabbedPane.addTab("Rooms", roomPanel);
        tabbedPane.addTab("Teachers", teacherPanel);
        tabbedPane.addTab("Classes", classPanel);

        add(tabbedPane);
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton addButton = new JButton("Add Schedule Entry");
        addButton.addActionListener(e -> showAddScheduleEntryDialog());
        panel.add(addButton, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton manageButton = new JButton("Manage Courses");
        manageButton.addActionListener(e -> showManageCoursesDialog());
        panel.add(manageButton, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton manageButton = new JButton("Manage Rooms");
        manageButton.addActionListener(e -> showManageRoomsDialog());
        panel.add(manageButton, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createTeacherPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton manageButton = new JButton("Manage Teachers");
        manageButton.addActionListener(e -> showManageTeachersDialog());
        panel.add(manageButton, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createClassPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton manageButton = new JButton("Manage Classes");
        manageButton.addActionListener(e -> showManageClassesDialog());
        panel.add(manageButton, BorderLayout.NORTH);
        return panel;
    }

    private void loadData() {
        try {
            courseOfferings = dataManager.getAllCourseOfferings();
            rooms = dataManager.getAllRooms();
            teachers = dataManager.getAllTeachers();
            classes = dataManager.getAllSchoolClasses();
            scheduleEntries = dataManager.getAllScheduleEntries(courseOfferings, rooms);

            timetablePanel.setEntries(scheduleEntries);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddScheduleEntryDialog() {
        try {
            AddEditEntryDialog dialog = new AddEditEntryDialog(this, courseOfferings, rooms, null);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                ScheduleEntry entry = dialog.getScheduleEntry();
                if (scheduler.canAddEntry(entry, scheduleEntries)) {
                    dataManager.addScheduleEntry(entry);
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Cannot add entry due to conflicts.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding schedule entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showManageCoursesDialog() {
        ManageCoursesDialog dialog = new ManageCoursesDialog(this, teachers, classes);
        dialog.setVisible(true);
        if (dialog.isDataChanged()) {
            loadData();
        }
    }

    private void showManageRoomsDialog() {
        ManageRoomsDialog dialog = new ManageRoomsDialog(this, dataManager);
        dialog.setVisible(true);
        loadData();
    }

    private void showManageTeachersDialog() {
        ManageTeachersDialog dialog = new ManageTeachersDialog(this, dataManager);
        dialog.setVisible(true);
        loadData();
    }

    private void showManageClassesDialog() {
        ManageClassesDialog dialog = new ManageClassesDialog(this, dataManager);
        dialog.setVisible(true);
        loadData();
    }

    private void showAddEditEntryDialog(ScheduleEntry entry) {
        try {
            AddEditEntryDialog dialog = new AddEditEntryDialog(this, courseOfferings, rooms, entry);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                ScheduleEntry newEntry = dialog.getScheduleEntry();
                if (newEntry != null) {
                    if (entry == null) {
                        if (scheduler.canAddEntry(newEntry, scheduleEntries)) {
                            dataManager.addScheduleEntry(newEntry);
                            loadData();
                        } else {
                            JOptionPane.showMessageDialog(this, "Cannot add entry due to conflicts.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        if (scheduler.canAddEntry(newEntry, scheduleEntries)) {
                            dataManager.updateScheduleEntry(newEntry);
                            loadData();
                        } else {
                            JOptionPane.showMessageDialog(this, "Cannot update entry due to conflicts.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error managing schedule entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainScheduleFrame().setVisible(true);
        });
    }
}