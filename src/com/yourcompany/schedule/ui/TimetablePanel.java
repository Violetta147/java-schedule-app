package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.*; // Import tất cả model

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
// import java.time.LocalTime; // Không còn dùng trực tiếp ở đây
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects; // Thêm import này

public class TimetablePanel extends JPanel {
    private static final int START_HOUR = 7; // Giờ bắt đầu hiển thị trên lưới
    private static final int END_HOUR = 18;   // Giờ kết thúc hiển thị trên lưới (đến 18:59)
    private static final int SLOT_HEIGHT = 50; // Chiều cao của mỗi "giờ" trên lưới
    // private static final int SLOT_WIDTH = 120; // Sẽ được tính toán động
    private static final DayOfWeek[] DAYS = {
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    };
    private static final int HEADER_HEIGHT = 30; // Chiều cao cho tiêu đề ngày
    private static final int TIME_LABEL_WIDTH = 40; // Chiều rộng cho nhãn thời gian
    private static final int GRID_MARGIN = 20; // Lề chung

    private List<ScheduleEntry> entries;
    private Map<Course, Color> courseColors = new HashMap<>();
    private final Color[] palette = {
        new Color(135, 206, 250), new Color(255, 182, 193), new Color(144, 238, 144),
        new Color(255, 255, 153), new Color(255, 160, 122), new Color(221, 160, 221),
        new Color(173, 216, 230), new Color(240, 128, 128), new Color(255, 218, 185)
    };

    private LocalDate currentWeekStart;
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");

    public TimetablePanel() {
        setCurrentWeek(LocalDate.now());
        // Kích thước sẽ được tính toán lại trong paintComponent,
        // nhưng có thể đặt một kích thước ban đầu hợp lý.
        setPreferredSize(new Dimension(TIME_LABEL_WIDTH + (120 * DAYS.length) + GRID_MARGIN * 2,
                                       HEADER_HEIGHT + (SLOT_HEIGHT * (END_HOUR - START_HOUR)) + GRID_MARGIN * 2));
        setToolTipText(""); // Kích hoạt tooltips
    }

    public void setCurrentWeek(LocalDate dateInWeek) {
        this.currentWeekStart = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        assignColors(); // Gán lại màu khi tuần thay đổi và entries có thể khác
        repaint();
    }

    public LocalDate getCurrentWeekStart() {
        return currentWeekStart;
    }

    public void nextWeek() {
        setCurrentWeek(currentWeekStart.plusWeeks(1));
    }

    public void previousWeek() {
        setCurrentWeek(currentWeekStart.minusWeeks(1));
    }

    public String getCurrentWeekDisplay() {
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        return String.format("Tuần %d (%s - %s %d)",
                currentWeekStart.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()),
                currentWeekStart.format(dayFormatter),
                weekEnd.format(dayFormatter),
                currentWeekStart.getYear()
        );
    }


    public void setEntries(List<ScheduleEntry> entries) {
        this.entries = entries;
        assignColors();
        repaint();
    }

    private void assignColors() {
        courseColors.clear();
        if (entries == null) return;
        int idx = 0;
        for (ScheduleEntry entry : entries) {
            Course course = entry.getCourse(); // Sử dụng getter tiện ích trong ScheduleEntry
            if (course != null && !courseColors.containsKey(course)) {
                courseColors.put(course, palette[idx % palette.length]);
                idx++;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = getWidth();
        // int panelHeight = getHeight(); // Không dùng trực tiếp panelHeight cho lưới

        int gridContentWidth = panelWidth - TIME_LABEL_WIDTH - GRID_MARGIN * 2;
        int slotWidth = gridContentWidth / DAYS.length; // Tính toán động slotWidth

        // Chiều cao cố định của lưới dựa trên số giờ và chiều cao slot
        int gridContentHeight = SLOT_HEIGHT * (END_HOUR - START_HOUR);

        // Tọa độ bắt đầu của lưới (sau lề và nhãn thời gian/tiêu đề ngày)
        int gridX = GRID_MARGIN + TIME_LABEL_WIDTH;
        int gridY = GRID_MARGIN + HEADER_HEIGHT;

        // Draw background cho toàn bộ panel
        g2.setColor(getBackground()); // Hoặc new Color(230, 230, 230)
        g2.fillRect(0, 0, panelWidth, getHeight());

        // Draw background cho khu vực lưới
        g2.setColor(new Color(245, 245, 250)); // Màu nền lưới
        g2.fillRect(gridX, gridY, gridContentWidth, gridContentHeight);

        // Draw grid lines
        g2.setColor(new Color(200, 200, 200)); // Màu đường kẻ lưới
        // Vertical lines (chia ngày)
        for (int i = 0; i <= DAYS.length; i++) {
            int x = gridX + i * slotWidth;
            g2.drawLine(x, gridY, x, gridY + gridContentHeight);
        }
        // Horizontal lines (chia giờ)
        for (int i = 0; i <= (END_HOUR - START_HOUR); i++) {
            int y = gridY + i * SLOT_HEIGHT;
            g2.drawLine(gridX, y, gridX + gridContentWidth, y);
        }

        // Draw day labels with dates (phía trên lưới)
        g2.setColor(Color.BLACK);
        Font originalFont = g2.getFont();
        Font dayHeaderFont = originalFont.deriveFont(Font.BOLD, originalFont.getSize() -1f); // Nhỏ hơn một chút
        g2.setFont(dayHeaderFont);
        FontMetrics fmHeader = g2.getFontMetrics();

        for (int i = 0; i < DAYS.length; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            String dayName = DAYS[i].toString().substring(0, 1) + DAYS[i].toString().substring(1,3).toLowerCase(); // Mon, Tue
            String dateStr = date.format(dayFormatter);
            String label = dayName + " " + dateStr;
            
            int labelWidth = fmHeader.stringWidth(label);
            g2.drawString(label, gridX + i * slotWidth + (slotWidth - labelWidth) / 2, GRID_MARGIN + fmHeader.getAscent());
        }

        // Draw time labels (bên trái lưới)
        g2.setFont(originalFont.deriveFont(originalFont.getSize() - 2f)); // Font nhỏ hơn cho giờ
        FontMetrics fmTime = g2.getFontMetrics();
        for (int i = 0; i < (END_HOUR - START_HOUR); i++) {
            int hour = START_HOUR + i;
            String time = String.format("%02d:00", hour);
            int timeWidth = fmTime.stringWidth(time);
            g2.drawString(time, GRID_MARGIN + (TIME_LABEL_WIDTH - timeWidth) / 2 -2 , gridY + i * SLOT_HEIGHT + fmTime.getAscent() + 3);
        }
        g2.setFont(originalFont); // Reset font

        // Draw entries
        if (entries != null) {
            for (ScheduleEntry entry : entries) {
                // Chỉ vẽ các entry thuộc tuần hiện tại
                if (entry.getDate() != null && isDateInCurrentWeek(entry.getDate())) {
                    drawEntry(g2, entry, gridX, gridY, slotWidth, SLOT_HEIGHT);
                }
            }
        }
    }

    private boolean isDateInCurrentWeek(LocalDate date) {
        if (date == null || currentWeekStart == null) return false;
        LocalDate weekEnd = currentWeekStart.plusDays(6); // Chủ nhật của tuần hiện tại
        return !date.isBefore(currentWeekStart) && !date.isAfter(weekEnd);
    }

 // Trong TimetablePanel.java, phương thức drawEntry

    private void drawEntry(Graphics2D g2, ScheduleEntry entry, int gridStartX, int gridStartY, int colWidth, int hourHeight) {
    	Font originalFontForEntry = g2.getFont();
    	
        LocalDateTime startDateTime = entry.getStartDateTime();
        LocalDateTime endDateTime = entry.getEndDateTime();

        if (startDateTime == null || endDateTime == null) return;

        LocalDate entryDate = entry.getDate();
        int dayIndex = entryDate.getDayOfWeek().getValue() - 1;

        double startHourFraction = startDateTime.getHour() + startDateTime.getMinute() / 60.0;
        double endHourFraction = endDateTime.getHour() + endDateTime.getMinute() / 60.0;

        if (endHourFraction <= START_HOUR || startHourFraction >= END_HOUR) return;
        
        double effectiveStartHour = Math.max(startHourFraction, START_HOUR);
        double effectiveEndHour = Math.min(endHourFraction, END_HOUR);

        if (effectiveEndHour <= effectiveStartHour) return; // Không có gì để vẽ

        int x = gridStartX + dayIndex * colWidth;
        int y = gridStartY + (int) ((effectiveStartHour - START_HOUR) * hourHeight);
        int entryHeight = (int) ((effectiveEndHour - effectiveStartHour) * hourHeight);

        // Đặt một chiều cao tối thiểu để trực quan hóa, nhưng text chỉ vẽ nếu đủ chỗ
        int visualMinHeight = 5; 
        if (entryHeight < visualMinHeight) entryHeight = visualMinHeight;

        Course course = entry.getCourse();
        Color baseColor = courseColors.getOrDefault(course, Color.LIGHT_GRAY);

        // Vẽ nền và viền
        g2.setColor(baseColor.darker());
        g2.fillRoundRect(x + 2, y + 2, colWidth - 4, entryHeight - 4, 8, 8);
        g2.setColor(baseColor);
        g2.fillRoundRect(x + 3, y + 3, colWidth - 6, entryHeight - 6, 6, 6);

        // Chỉ vẽ text nếu chiều cao đủ cho ít nhất một phần của dòng đầu tiên
        Font entryFont = originalFontForEntry.deriveFont(10f); // Tạo font mới dựa trên font gốc của context
        g2.setFont(entryFont); // Set font mới để vẽ text của entry
        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = fm.getHeight();
        int textPaddingX = 5;
        int textPaddingY = 4; // Padding từ đỉnh của ô entry
        int textX = x + textPaddingX;
        int currentTextY = y + fm.getAscent() + textPaddingY;
        int maxWidth = colWidth - (2 * textPaddingX);

        // Kiểm tra xem có đủ chỗ cho ít nhất dòng đầu tiên không
        if (entryHeight >= lineHeight * 0.8 + (2 * textPaddingY)) { // Cần đủ không gian cho text và padding
        	g2.setColor(Color.BLACK); 

            String courseCode = (course != null) ? course.getCourseCode() : "N/A";
            g2.drawString(getEllipsisText(courseCode, maxWidth, fm), textX, currentTextY);
            currentTextY += lineHeight;

            // Kiểm tra đủ chỗ cho dòng thứ hai (Phòng)
            if (entryHeight >= (lineHeight * 2 * 0.8) + (2 * textPaddingY) && (currentTextY + fm.getDescent() <= y + entryHeight - textPaddingY)) {
                Room room = entry.getRoom();
                String roomName = (room != null) ? room.getRoomName() : "N/A";
                g2.drawString(getEllipsisText("P: " + roomName, maxWidth, fm), textX, currentTextY);
                currentTextY += lineHeight;

                // Kiểm tra đủ chỗ cho dòng thứ ba (Giáo viên)
                if (entryHeight >= (lineHeight * 3 * 0.8) + (2 * textPaddingY) && (currentTextY + fm.getDescent() <= y + entryHeight - textPaddingY)) {
                    Teacher teacher = (entry.getCourseOffering() != null) ? entry.getCourseOffering().getTeacher() : null;
                    String teacherName = (teacher != null) ? teacher.getName() : "N/A";
                    // Lấy tên viết tắt hoặc một phần tên để ngắn gọn
                    String displayTeacherName = teacherName;
                    if (teacherName.length() > 10) { // Ví dụ: chỉ hiển thị 10 ký tự đầu
                        displayTeacherName = teacherName.substring(0, 10) + "...";
                    }
                    g2.drawString(getEllipsisText("GV: " + displayTeacherName, maxWidth, fm), textX, currentTextY);
                }
            }
        }
        g2.setFont(originalFontForEntry);
    }

    private String getEllipsisText(String text, int maxWidth, FontMetrics fm) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int len = text.length();
        // Lặp để cắt bớt ký tự cho đến khi vừa
        while (len > 0 && fm.stringWidth(text.substring(0, len) + ellipsis) > maxWidth) {
            len--;
        }
        return (len > 0) ? text.substring(0, len) + ellipsis : ellipsis;
    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent event) {
        if (entries == null || currentWeekStart == null) return null;

        int mx = event.getX();
        int my = event.getY();

        int panelWidth = getWidth();
        int gridContentWidth = panelWidth - TIME_LABEL_WIDTH - GRID_MARGIN * 2;
        int colWidth = gridContentWidth / DAYS.length;
        int gridStartX = GRID_MARGIN + TIME_LABEL_WIDTH;
        int gridStartY = GRID_MARGIN + HEADER_HEIGHT;


        for (ScheduleEntry entry : entries) {
            if (entry.getDate() == null || !isDateInCurrentWeek(entry.getDate())) continue;

            LocalDateTime startDateTime = entry.getStartDateTime();
            LocalDateTime endDateTime = entry.getEndDateTime();
            if (startDateTime == null || endDateTime == null) continue;

            LocalDate entryDate = entry.getDate();
            int dayIndex = entryDate.getDayOfWeek().getValue() - 1;

            double startHourFraction = startDateTime.getHour() + startDateTime.getMinute() / 60.0;
            double endHourFraction = endDateTime.getHour() + endDateTime.getMinute() / 60.0;
            
            double effectiveStartHour = Math.max(startHourFraction, START_HOUR);
            double effectiveEndHour = Math.min(endHourFraction, END_HOUR);

            if (effectiveEndHour <= effectiveStartHour) continue; // Không hiển thị nếu nằm ngoài khung giờ

            int x = gridStartX + dayIndex * colWidth;
            int y = gridStartY + (int) ((effectiveStartHour - START_HOUR) * SLOT_HEIGHT);
            int entryHeight = (int) ((effectiveEndHour - effectiveStartHour) * SLOT_HEIGHT);
            
            // Hit detection: kiểm tra xem chuột có nằm trong hình chữ nhật của entry không
            if (mx >= x && mx <= x + colWidth && my >= y && my <= y + entryHeight) {
                Course course = entry.getCourse();
                Room room = entry.getRoom();
                Teacher teacher = (entry.getCourseOffering() != null) ? entry.getCourseOffering().getTeacher() : null;
                SchoolClass schoolClass = entry.getSchoolClass(); // Lấy trực tiếp từ entry

                String courseName = (course != null) ? course.getCourseName() : "N/A";
                String courseCode = (course != null) ? course.getCourseCode() : "N/A";
                String roomName = (room != null) ? room.getRoomName() : "N/A";
                String teacherName = (teacher != null) ? teacher.getName() : "N/A";
                String className = (schoolClass != null) ? schoolClass.getName() : "N/A"; // SchoolClass đã có getName()

                return String.format("<html><b>%s (%s)</b><br>" +
                                     "Thời gian: %s - %s (%s %s)<br>" +
                                     "Phòng: %s<br>" +
                                     "GV: %s<br>" +
                                     "Lớp: %s</html>",
                        courseName, courseCode,
                        startDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        endDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        entry.getDate().getDayOfWeek().toString().substring(0,1).toUpperCase() + entry.getDate().getDayOfWeek().toString().substring(1).toLowerCase(),
                        entry.getDate().format(dayFormatter),
                        roomName,
                        teacherName,
                        className);
            }
        }
        return null; // Không có tooltip nếu không trỏ vào entry nào
    }
}