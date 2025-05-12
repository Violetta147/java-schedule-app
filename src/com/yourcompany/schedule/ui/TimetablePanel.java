package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.ScheduleEntry;
import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.Room;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TimetablePanel extends JPanel {
    private static final int START_HOUR = 0; // Start from midnight
    private static final int END_HOUR = 24; // End at midnight
    private static final int SLOT_HEIGHT = 40;
    private static final int SLOT_WIDTH = 120;
    private static final DayOfWeek[] DAYS = {
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    };
    private static final int GRID_MARGIN = 20;
    private List<ScheduleEntry> entries;
    private Map<Course, Color> courseColors = new HashMap<>();
    private Color[] palette = {new Color(135,206,250), new Color(255,182,193), new Color(144,238,144), new Color(255,255,153), new Color(255,160,122), new Color(221,160,221)};
    
    // Week selection properties
    private LocalDate currentWeekStart;
    private DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");

    public TimetablePanel() {
        // Initialize with current week
        setCurrentWeek(LocalDate.now());
        
        // Set a larger preferred size to accommodate all hours
        setPreferredSize(new Dimension(SLOT_WIDTH * DAYS.length + 60, SLOT_HEIGHT * (END_HOUR - START_HOUR) + 40));
        setToolTipText("");
    }
    
    public void setCurrentWeek(LocalDate date) {
        // Find the Monday of the week containing the given date
        this.currentWeekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
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
    
    public int getCurrentWeekNumber() {
        return currentWeekStart.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
    }
    
    public int getCurrentYear() {
        return currentWeekStart.getYear();
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
            Course c = entry.getCourse();
            if (c != null && !courseColors.containsKey(c)) {
                courseColors.put(c, palette[idx % palette.length]);
                idx++;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        int gridWidth = width - 2 * GRID_MARGIN - 40;
        int gridHeight = SLOT_HEIGHT * (END_HOUR - START_HOUR); // Fixed grid height based on hours
        int slotWidth = gridWidth / DAYS.length;
        int slotHeight = SLOT_HEIGHT; // Fixed slot height
        int minBlockHeight = 18;
        
        // Draw background
        g2.setColor(new Color(245, 245, 250));
        g2.fillRect(GRID_MARGIN, GRID_MARGIN, gridWidth + 40, gridHeight);
        
        // Draw grid
        g2.setColor(new Color(200, 200, 200));
        for (int i = 0; i <= DAYS.length; i++) {
            int x = GRID_MARGIN + 40 + i * slotWidth;
            g2.drawLine(x, GRID_MARGIN, x, GRID_MARGIN + gridHeight);
        }
        for (int i = 0; i <= END_HOUR - START_HOUR; i++) {
            int y = GRID_MARGIN + i * slotHeight;
            g2.drawLine(GRID_MARGIN + 40, y, GRID_MARGIN + 40 + DAYS.length * slotWidth, y);
        }
        
        // Draw day labels with dates
        g2.setColor(Color.BLACK);
        Font originalFont = g2.getFont();
        Font boldFont = originalFont.deriveFont(Font.BOLD);
        
        for (int i = 0; i < DAYS.length; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            String dayName = DAYS[i].toString().substring(0, 1) + DAYS[i].toString().substring(1).toLowerCase();
            String dateStr = date.format(dayFormatter);
            String label = dayName + " " + dateStr;
            
            g2.setFont(boldFont);
            g2.drawString(label, GRID_MARGIN + 40 + i * slotWidth + 10, GRID_MARGIN - 5);
            g2.setFont(originalFont);
        }
        
        // Draw time labels
        for (int i = 0; i < END_HOUR - START_HOUR; i++) {
            String time = String.format("%02d:00", START_HOUR + i);
            g2.drawString(time, GRID_MARGIN, GRID_MARGIN + i * slotHeight + 15);
        }
        
        // Draw entries that fall within the current week
        if (entries != null) {
            for (ScheduleEntry entry : entries) {
                LocalDateTime entryDateTime = entry.getStartDateTime();
                LocalDate entryDate = entryDateTime.toLocalDate();
                
                // Only draw entries that fall within the displayed week
                if (isDateInCurrentWeek(entryDate)) {
                    drawEntry(g2, entry, slotWidth, slotHeight, minBlockHeight);
                }
            }
        }
    }
    
    private boolean isDateInCurrentWeek(LocalDate date) {
        // Check if the date falls within the current week (Monday to Sunday)
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        return !date.isBefore(currentWeekStart) && !date.isAfter(weekEnd);
    }

    private void drawEntry(Graphics2D g2, ScheduleEntry entry, int slotWidth, int slotHeight, int minBlockHeight) {
        LocalDateTime start = entry.getStartDateTime();
        LocalDateTime end = entry.getEndDateTime();
        LocalDate entryDate = start.toLocalDate();
        
        // Calculate days since start of week
        int daysSinceWeekStart = (int) java.time.temporal.ChronoUnit.DAYS.between(currentWeekStart, entryDate);
        if (daysSinceWeekStart < 0 || daysSinceWeekStart >= DAYS.length) return;
        
        double startFrac = (start.getHour() + start.getMinute()/60.0 - START_HOUR);
        double endFrac = (end.getHour() + end.getMinute()/60.0 - START_HOUR);
        int y = GRID_MARGIN + (int)(startFrac * slotHeight);
        int height = Math.max((int)((endFrac - startFrac) * slotHeight), minBlockHeight);
        int x = GRID_MARGIN + 40 + daysSinceWeekStart * slotWidth;
        Color color = courseColors.getOrDefault(entry.getCourse(), Color.LIGHT_GRAY);
        g2.setColor(color);
        g2.fillRoundRect(x+4, y+4, slotWidth-8, height-8, 10, 10);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(x+4, y+4, slotWidth-8, height-8, 10, 10);
        
        // Draw course name, teacher, class and room information
        g2.setColor(Color.BLACK);
        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = fm.getHeight();
        int textX = x + 12;
        int textY = y + 20;
        int maxWidth = slotWidth - 16;
        
        // Course name
        String courseName = entry.getCourse() != null ? entry.getCourse().getCourseName() : "";
        String displayCourseName = getEllipsisText(courseName, maxWidth, fm);
        g2.drawString(displayCourseName, textX, textY);
        
        // Only draw additional info if the block is tall enough
        if (height >= minBlockHeight + lineHeight * 2) {
            // Room
            String roomName = entry.getRoom() != null ? entry.getRoom().getRoomName() : "";
            String displayRoomName = getEllipsisText("Room: " + roomName, maxWidth, fm);
            g2.drawString(displayRoomName, textX, textY + lineHeight);
            
            // Teacher - only if height permits
            if (height >= minBlockHeight + lineHeight * 3) {
                String teacherName = entry.getCourse() != null && entry.getCourse().getTeacher() != null ? 
                                    entry.getCourse().getTeacher().getName() : "";
                String displayTeacherName = getEllipsisText("Teacher: " + teacherName, maxWidth, fm);
                g2.drawString(displayTeacherName, textX, textY + lineHeight * 2);
                
                // Class - only if height permits
                if (height >= minBlockHeight + lineHeight * 4) {
                    String className = entry.getCourse() != null && entry.getCourse().getSchoolClass() != null ? 
                                      entry.getCourse().getSchoolClass().getName() : "";
                    String displayClassName = getEllipsisText("Class: " + className, maxWidth, fm);
                    g2.drawString(displayClassName, textX, textY + lineHeight * 3);
                }
            }
        }
    }
    
    // Helper method to truncate text with ellipsis if too long
    private String getEllipsisText(String text, int maxWidth, FontMetrics fm) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        
        String ellipsis = "...";
        int len = text.length();
        while (len > 0 && fm.stringWidth(text.substring(0, len) + ellipsis) > maxWidth) {
            len--;
        }
        return text.substring(0, len) + ellipsis;
    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent event) {
        if (entries == null) return null;
        int mx = event.getX();
        int my = event.getY();
        
        for (ScheduleEntry entry : entries) {
            LocalDateTime start = entry.getStartDateTime();
            LocalDateTime end = entry.getEndDateTime();
            LocalDate entryDate = start.toLocalDate();
            
            // Skip entries not in the current week
            if (!isDateInCurrentWeek(entryDate)) continue;
            
            // Calculate days since start of week
            int daysSinceWeekStart = (int) java.time.temporal.ChronoUnit.DAYS.between(currentWeekStart, entryDate);
            if (daysSinceWeekStart < 0 || daysSinceWeekStart >= DAYS.length) continue;
            
            int y = GRID_MARGIN + (int)((start.getHour() + start.getMinute()/60.0 - START_HOUR) * SLOT_HEIGHT);
            int height = (int)(((end.toLocalTime().toSecondOfDay() - start.toLocalTime().toSecondOfDay()) / 3600.0) * SLOT_HEIGHT);
            int x = GRID_MARGIN + 40 + daysSinceWeekStart * SLOT_WIDTH;
            
            if (mx >= x+4 && mx <= x+SLOT_WIDTH-4 && my >= y+4 && my <= y+height-4) {
                String courseName = entry.getCourse() != null ? entry.getCourse().getCourseName() : "No Course";
                String roomName = entry.getRoom() != null ? entry.getRoom().getRoomName() : "No Room";
                String teacherName = entry.getCourse() != null && entry.getCourse().getTeacher() != null ? 
                                    entry.getCourse().getTeacher().getName() : "No Teacher";
                String className = entry.getCourse() != null && entry.getCourse().getSchoolClass() != null ? 
                                  entry.getCourse().getSchoolClass().getName() : "No Class";
                
                return String.format("<html>%s<br>%s %s<br>%s - %s<br>Room: %s<br>Teacher: %s<br>Class: %s</html>", 
                    courseName, 
                    entryDate.getDayOfWeek(), 
                    entryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    start.toLocalTime(), 
                    end.toLocalTime(), 
                    roomName,
                    teacherName,
                    className);
            }
        }
        return null;
    }
} 