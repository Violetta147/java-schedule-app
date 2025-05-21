package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.*;
import com.yourcompany.schedule.service.ScheduleService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener; // Cần import lại
import java.util.Objects;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainScheduleFrame extends JFrame {
    private SchedulePanel schedulePanel;
    private TimetablePanel timetableGridPanel;
    private ScheduleService scheduleService;

    private JLabel weekLabel;
    private final DateTimeFormatter weekDisplayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private JButton addEntryButton;
    private JButton editEntryButton;
    private JButton deleteEntryButton;

    private JButton manageAcaYearsButton;
    private JButton manageCoursesButton;
    private JButton manageRoomsButton;
    private JButton manageClassesButton;
    private JButton manageTeachersButton;
    private JButton manageOfferingsButton;

    public enum TimetableFilterType {
        TEACHER("Theo Giáo Viên"),
        ROOM("Theo Phòng Học"),
        CLASS("Theo Lớp Học");

        private final String displayName;
        TimetableFilterType(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    private JComboBox<TimetableFilterType> mainFilterTypeComboBox;
    private JComboBox<Object> mainEntityFilterComboBox;
    private JButton clearMainFilterButton;

    private TimetableFilterType currentMainFilterType = TimetableFilterType.TEACHER;
    private Object selectedMainFilterEntity = null;

    private List<Teacher> allTeachers;
    private List<Room> allRooms;
    private List<SchoolClass> allSchoolClasses;


    private ActionListener mainEntityFilterComboBoxListener;

    public MainScheduleFrame() {
        this.scheduleService = new ScheduleService();
        setTitle("Quản Lý Lịch Trình");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setMinimumSize(new Dimension(900, 650));

        setLayout(new BorderLayout(5,5));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        initComponents();
        loadAndRefreshAllData();
    }

    private void initComponents() {
    	timetableGridPanel = new TimetablePanel();
        schedulePanel = new SchedulePanel();

        JPanel topControlPanelForTimetableTab = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTopControls = new GridBagConstraints();
        gbcTopControls.insets = new Insets(5, 5, 5, 5);

        JPanel gridFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        gridFilterPanel.add(new JLabel("Lọc theo:"));
        mainFilterTypeComboBox = new JComboBox<>(TimetableFilterType.values());
        mainFilterTypeComboBox.addActionListener(e -> onMainFilterTypeChanged());
        gridFilterPanel.add(mainFilterTypeComboBox);

        gridFilterPanel.add(new JLabel("Đối tượng:"));
        mainEntityFilterComboBox = new JComboBox<>();
        mainEntityFilterComboBox.setPreferredSize(new Dimension(180, mainEntityFilterComboBox.getPreferredSize().height));

        mainEntityFilterComboBoxListener = e -> onMainEntityFilterChanged();
        mainEntityFilterComboBox.addActionListener(mainEntityFilterComboBoxListener);
        gridFilterPanel.add(mainEntityFilterComboBox);

        clearMainFilterButton = new JButton("Xóa Lựa Chọn");
        clearMainFilterButton.setToolTipText("Đặt lại bộ lọc về mặc định (Theo Giáo Viên, giáo viên đầu tiên)");
        clearMainFilterButton.addActionListener(e -> clearMainFilterAction());
        gridFilterPanel.add(clearMainFilterButton);

        gbcTopControls.gridx = 0; gbcTopControls.gridy = 0; gbcTopControls.weightx = 0;
        gbcTopControls.anchor = GridBagConstraints.WEST; gbcTopControls.fill = GridBagConstraints.NONE;
        topControlPanelForTimetableTab.add(gridFilterPanel, gbcTopControls);

        JPanel weekNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton prevWeekButton = new JButton("Tuần Trước");
        JButton currentWeekButton = new JButton("Tuần Hiện Tại");
        JButton nextWeekButton = new JButton("Tuần Tiếp Theo");
        weekLabel = new JLabel(); weekLabel.setFont(weekLabel.getFont().deriveFont(Font.BOLD));
        weekNavPanel.add(prevWeekButton); weekNavPanel.add(currentWeekButton);
        weekNavPanel.add(weekLabel); weekNavPanel.add(nextWeekButton);
        gbcTopControls.gridx = 1; gbcTopControls.gridy = 0; gbcTopControls.weightx = 1.0;
        gbcTopControls.anchor = GridBagConstraints.CENTER; gbcTopControls.fill = GridBagConstraints.HORIZONTAL;
        topControlPanelForTimetableTab.add(weekNavPanel, gbcTopControls);

        updateWeekDisplayLabel();
        prevWeekButton.addActionListener(e -> navigateWeek(-1));
        currentWeekButton.addActionListener(e -> navigateToDate(LocalDate.now()));
        nextWeekButton.addActionListener(e -> navigateWeek(1));

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel timetableTabPanel = new JPanel(new BorderLayout());
        timetableTabPanel.add(topControlPanelForTimetableTab, BorderLayout.NORTH);
        JScrollPane timetableScrollPane = new JScrollPane(timetableGridPanel);
        timetableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        timetableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        timetableTabPanel.add(timetableScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Lịch Lưới (Tuần)", timetableTabPanel);
        tabbedPane.addTab("Lịch Bảng (Chi Tiết)", schedulePanel);
        add(tabbedPane, BorderLayout.CENTER);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addEntryButton = new JButton("Thêm Lịch Học"); editEntryButton = new JButton("Sửa Lịch Học");
        deleteEntryButton = new JButton("Xóa Lịch Học");
        actionButtonPanel.add(addEntryButton); actionButtonPanel.add(editEntryButton); actionButtonPanel.add(deleteEntryButton);

        JPanel managementButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        managementButtonPanel.setBorder(BorderFactory.createTitledBorder("Quản Lý Dữ Liệu"));
        manageAcaYearsButton = new JButton("Năm Học"); manageCoursesButton = new JButton("Môn Học");
        manageRoomsButton = new JButton("Phòng Học"); manageClassesButton = new JButton("Lớp Học");
        manageTeachersButton = new JButton("Giáo Viên"); manageOfferingsButton = new JButton("Phân Công GD");
        managementButtonPanel.add(manageAcaYearsButton); managementButtonPanel.add(manageCoursesButton);
        managementButtonPanel.add(manageRoomsButton); managementButtonPanel.add(manageClassesButton);
        managementButtonPanel.add(manageTeachersButton); managementButtonPanel.add(manageOfferingsButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(actionButtonPanel, BorderLayout.CENTER); southPanel.add(managementButtonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar(); JMenu fileMenu = new JMenu("Tệp");
        JMenuItem refreshDataItem = new JMenuItem("Làm Mới Dữ Liệu");
        refreshDataItem.addActionListener(e -> loadAndRefreshAllData());
        fileMenu.add(refreshDataItem); fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Thoát"); exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem); menuBar.add(fileMenu); setJMenuBar(menuBar);

        addEntryButton.addActionListener(this::addScheduleEntryAction);
        editEntryButton.addActionListener(this::editScheduleEntryAction);
        deleteEntryButton.addActionListener(this::deleteScheduleEntryAction);
        manageAcaYearsButton.addActionListener(e -> { new ManageAcaYearsDialog(this, scheduleService).setVisible(true); loadAndRefreshAllData(); });
        manageCoursesButton.addActionListener(e -> { new ManageCoursesDialog(this, scheduleService).setVisible(true); loadAndRefreshAllData(); });
        manageRoomsButton.addActionListener(e -> { new ManageRoomsDialog(this, scheduleService).setVisible(true); loadAndRefreshAllData(); });
        manageTeachersButton.addActionListener(e -> { new ManageTeachersDialog(this, scheduleService).setVisible(true); loadAndRefreshAllData(); });
        manageClassesButton.addActionListener(e -> { new ManageClassesDialog(this, scheduleService).setVisible(true); loadAndRefreshAllData(); });
        manageOfferingsButton.addActionListener(e -> { new ManageCourseOfferingsDialog(this, scheduleService).setVisible(true); loadAndRefreshAllData(); });

    }

    private void onMainFilterTypeChanged() {
        currentMainFilterType = (TimetableFilterType) mainFilterTypeComboBox.getSelectedItem();

        updateMainEntityFilterComboBox();

        refreshTimetableWithMainFilter();
    }

    private void onMainEntityFilterChanged() {

        Object currentComboSelection = mainEntityFilterComboBox.getSelectedItem();
        int selectedIndex = mainEntityFilterComboBox.getSelectedIndex();

        if (mainEntityFilterComboBox.isEnabled()) {
            Object newSelectedEntity = null;
            if (selectedIndex > 0) {
                newSelectedEntity = currentComboSelection;
            }

            if (!Objects.equals(selectedMainFilterEntity, newSelectedEntity)) {
                selectedMainFilterEntity = newSelectedEntity;
                refreshTimetableWithMainFilter();
            }
        }
    }

    private void clearMainFilterAction() {
        if (mainFilterTypeComboBox.getItemCount() > 0) {
            mainFilterTypeComboBox.setSelectedItem(TimetableFilterType.TEACHER);
        }
    }

    private void updateMainEntityFilterComboBox() {
        if (mainEntityFilterComboBoxListener != null) {
            mainEntityFilterComboBox.removeActionListener(mainEntityFilterComboBoxListener);
        }

        mainEntityFilterComboBox.removeAllItems();
        mainEntityFilterComboBox.setEnabled(true);

        if (currentMainFilterType == null && TimetableFilterType.values().length > 0) {
            currentMainFilterType = TimetableFilterType.values()[0]; // Fallback
        }

        String placeholder = "";
        List<?> entities = null;

        switch (currentMainFilterType) {
            case TEACHER:
                placeholder = "Chọn Giáo Viên...";
                entities = allTeachers;
                break;
            case ROOM:
                placeholder = "Chọn Phòng Học...";
                entities = allRooms;
                break;
            case CLASS:
                placeholder = "Chọn Lớp Học...";
                entities = allSchoolClasses;
                break;
            default:
                mainEntityFilterComboBox.addItem("Lỗi: Loại lọc không xác định");
                mainEntityFilterComboBox.setEnabled(false);
                selectedMainFilterEntity = null;
                if (mainEntityFilterComboBoxListener != null) {
                    mainEntityFilterComboBox.addActionListener(mainEntityFilterComboBoxListener);
                }
                return;
        }

        mainEntityFilterComboBox.addItem(placeholder);
        if (entities != null) {
            for (Object entity : entities) {
                mainEntityFilterComboBox.addItem(entity);
            }
        }

        if (mainEntityFilterComboBox.getItemCount() > 1) {
            mainEntityFilterComboBox.setSelectedIndex(1);
            selectedMainFilterEntity = mainEntityFilterComboBox.getSelectedItem();
        } else {
            mainEntityFilterComboBox.setSelectedIndex(0);
            selectedMainFilterEntity = null;
        }

        if (mainEntityFilterComboBoxListener != null) {
            mainEntityFilterComboBox.addActionListener(mainEntityFilterComboBoxListener);
        }
    }

    private void refreshTimetableWithMainFilter() {
        System.out.println("Refreshing TIMETABLE GRID. Filter: " + currentMainFilterType + ", Entity: " + selectedMainFilterEntity);
        List<ScheduleEntry> allEntriesFromService = scheduleService.getAllScheduleEntries();
        List<ScheduleEntry> filteredEntriesForTimetable;

        if (selectedMainFilterEntity == null) {
            filteredEntriesForTimetable = new ArrayList<>(allEntriesFromService);
            System.out.println("Timetable grid: No specific entity selected. Showing all entries for the week.");
        } else {
            filteredEntriesForTimetable = allEntriesFromService.stream()
                .filter(entry -> {
                    switch (currentMainFilterType) {
                        case TEACHER:
                            return entry.getCourseOffering() != null &&
                                   entry.getCourseOffering().getTeacher() != null &&
                                   Objects.equals(entry.getCourseOffering().getTeacher(), selectedMainFilterEntity);
                        case ROOM:
                            return entry.getRoom() != null && Objects.equals(entry.getRoom(), selectedMainFilterEntity);
                        case CLASS:
                            return entry.getSchoolClass() != null && Objects.equals(entry.getSchoolClass(), selectedMainFilterEntity);
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
             System.out.println("Timetable grid: Filtered by " + currentMainFilterType + " - " + selectedMainFilterEntity);
        }

        timetableGridPanel.setEntries(filteredEntriesForTimetable);
        System.out.println("Timetable grid refreshed. Filtered entry count for grid: " + filteredEntriesForTimetable.size());
    }

    private void loadFilterEntityData() {
		allTeachers = scheduleService.getAllTeachers();
		allRooms = scheduleService.getAllRooms();
		allSchoolClasses = scheduleService.getAllSchoolClasses();
        System.out.println("Filter entity data loaded. Teachers: " + (allTeachers != null ? allTeachers.size() : 0) +
                           ", Rooms: " + (allRooms != null ? allRooms.size() : 0) +
                           ", Classes: " + (allSchoolClasses != null ? allSchoolClasses.size() : 0));
	}

    private void navigateWeek(int weekOffset) {
        LocalDate newWeekStart = timetableGridPanel.getCurrentWeekStart().plusWeeks(weekOffset);
        navigateToDate(newWeekStart);
    }

    private void navigateToDate(LocalDate date) {
        timetableGridPanel.setCurrentWeek(date);
        schedulePanel.setCurrentWeek(date);
        updateWeekDisplayLabel();
        refreshScheduleViews();
    }

    private void updateWeekDisplayLabel() {
        if (weekLabel == null || timetableGridPanel == null) return;
        LocalDate current = timetableGridPanel.getCurrentWeekStart();
        String startDate = current.format(weekDisplayFormatter);
        String endDate = current.plusDays(6).format(weekDisplayFormatter);
        int weekNumber = current.get(java.time.temporal.WeekFields.of(java.util.Locale.getDefault()).weekOfWeekBasedYear());
        weekLabel.setText(String.format("Tuần %d: %s - %s (%d)", weekNumber, startDate, endDate, current.getYear()));
    }

    private void loadAndRefreshAllData() {
        System.out.println("Loading all data from service...");
        loadFilterEntityData();

        mainFilterTypeComboBox.setSelectedItem(currentMainFilterType);

        updateMainEntityFilterComboBox();

        List<ScheduleEntry> allEntriesFromService = scheduleService.getAllScheduleEntries();
        schedulePanel.setScheduleEntries(allEntriesFromService);
        System.out.println("SchedulePanel (table) updated with " + allEntriesFromService.size() + " total entries.");

        refreshTimetableWithMainFilter();

        updateWeekDisplayLabel();
        System.out.println("All data refresh complete.");
    }

    private void refreshScheduleViews() {
        System.out.println("Refreshing schedule views...");
        List<ScheduleEntry> allEntriesFromService = scheduleService.getAllScheduleEntries();

        schedulePanel.setScheduleEntries(allEntriesFromService); // Lịch bảng luôn nhận toàn bộ
        System.out.println("SchedulePanel (table) refreshed with " + allEntriesFromService.size() + " total entries.");

        refreshTimetableWithMainFilter();

        updateWeekDisplayLabel();
        System.out.println("Schedule views refreshed.");
    }

    private void addScheduleEntryAction(ActionEvent e) {
        List<CourseOffering> offerings = scheduleService.getAllCourseOfferings();
        List<SchoolClass> classes = scheduleService.getAllSchoolClasses();
        List<Room> rooms = scheduleService.getAllRooms();
        List<AcaYear> acaYears = scheduleService.getAllAcaYears();

        if (offerings.isEmpty() || classes.isEmpty() || rooms.isEmpty() || acaYears.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Không đủ dữ liệu nền (Phân công, Lớp, Phòng, Năm học) để thêm lịch học.\n" +
                "Vui lòng thêm dữ liệu này trước.", "Thiếu Dữ Liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AddEditEntryDialog dialog = new AddEditEntryDialog(this, null, offerings, classes, rooms, acaYears);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            ScheduleEntry newEntry = dialog.getEntryData();
            if (newEntry != null) {
                Optional<ScheduleEntry> addedEntryOpt = scheduleService.createAndAddScheduleEntry(
                        newEntry.getCourseOffering(), newEntry.getSchoolClass(), newEntry.getRoom(),
                        newEntry.getAcaYear(), newEntry.getDate(), newEntry.getStartPeriod(), newEntry.getEndPeriod()
                );
                if (addedEntryOpt.isPresent()) {
                    JOptionPane.showMessageDialog(this, "Thêm lịch học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    loadAndRefreshAllData();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể thêm lịch học. Đã có lỗi hoặc xung đột.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void editScheduleEntryAction(ActionEvent e) {
        ScheduleEntry selectedEntry = schedulePanel.getSelectedEntry();
        if (selectedEntry == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một mục lịch học từ bảng để sửa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<CourseOffering> offerings = scheduleService.getAllCourseOfferings();
        List<SchoolClass> classes = scheduleService.getAllSchoolClasses();
        List<Room> rooms = scheduleService.getAllRooms();
        List<AcaYear> acaYears = scheduleService.getAllAcaYears();

        AddEditEntryDialog dialog = new AddEditEntryDialog(this, selectedEntry, offerings, classes, rooms, acaYears);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            ScheduleEntry updatedEntryData = dialog.getEntryData();
            if (updatedEntryData != null) {
                boolean success = scheduleService.updateScheduleEntry(
                        selectedEntry.getEntryId(),
                        updatedEntryData.getCourseOffering(), updatedEntryData.getSchoolClass(), updatedEntryData.getRoom(),
                        updatedEntryData.getAcaYear(), updatedEntryData.getDate(),
                        updatedEntryData.getStartPeriod(), updatedEntryData.getEndPeriod()
                );
                if (success) {
                    JOptionPane.showMessageDialog(this, "Cập nhật lịch học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                    loadAndRefreshAllData();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể cập nhật lịch học. Đã có lỗi hoặc xung đột.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteScheduleEntryAction(ActionEvent e) {
        ScheduleEntry selectedEntry = schedulePanel.getSelectedEntry();
        if (selectedEntry == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một mục lịch học từ bảng để xóa.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa mục lịch học này không?\n" + selectedEntry.toString(),
                "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = scheduleService.deleteScheduleEntry(selectedEntry.getEntryId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa lịch học thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                loadAndRefreshAllData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa lịch học.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new MainScheduleFrame().setVisible(true);
        });
    }
}