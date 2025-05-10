package com.yourcompany.schedule.ui;

import com.yourcompany.schedule.model.ScheduleEntry;
import com.yourcompany.schedule.model.Course;
import com.yourcompany.schedule.model.Room;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class TimetablePanel extends JPanel {
    private static final int START_HOUR = 8;
    private static final int END_HOUR = 18;
    private static final int SLOT_HEIGHT = 40;
    private static final int SLOT_WIDTH = 120;
    private static final DayOfWeek[] DAYS = {
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    };
    private static final int GRID_MARGIN = 20;
    private List<ScheduleEntry> entries;
    private Map<Course, Color> courseColors = new HashMap<>();
    private Color[] palette = {new Color(135,206,250), new Color(255,182,193), new Color(144,238,144), new Color(255,255,153), new Color(255,160,122), new Color(221,160,221)};

    public TimetablePanel() {
        setPreferredSize(new Dimension(SLOT_WIDTH * DAYS.length + 60, SLOT_HEIGHT * (END_HOUR - START_HOUR) + 40));
        setToolTipText("");
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
        int gridHeight = height - 2 * GRID_MARGIN;
        int slotWidth = gridWidth / DAYS.length;
        int slotHeight = gridHeight / (END_HOUR - START_HOUR);
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
        // Draw day labels
        g2.setColor(Color.BLACK);
        for (int i = 0; i < DAYS.length; i++) {
            String day = DAYS[i].toString().substring(0, 1) + DAYS[i].toString().substring(1).toLowerCase();
            g2.drawString(day, GRID_MARGIN + 40 + i * slotWidth + 10, GRID_MARGIN - 5);
        }
        // Draw time labels
        for (int i = 0; i < END_HOUR - START_HOUR; i++) {
            String time = String.format("%02d:00", START_HOUR + i);
            g2.drawString(time, GRID_MARGIN, GRID_MARGIN + i * slotHeight + 30);
        }
        // Draw entries
        if (entries != null) {
            for (ScheduleEntry entry : entries) {
                drawEntry(g2, entry, slotWidth, slotHeight, minBlockHeight);
            }
        }
    }

    private void drawEntry(Graphics2D g2, ScheduleEntry entry, int slotWidth, int slotHeight, int minBlockHeight) {
        LocalDateTime start = entry.getStartDateTime();
        LocalDateTime end = entry.getEndDateTime();
        DayOfWeek day = start.getDayOfWeek();
        int dayIdx = day.getValue() - 1; // Monday=0
        if (dayIdx < 0 || dayIdx >= DAYS.length) return;
        double startFrac = (start.getHour() + start.getMinute()/60.0 - START_HOUR);
        double endFrac = (end.getHour() + end.getMinute()/60.0 - START_HOUR);
        int y = GRID_MARGIN + (int)(startFrac * slotHeight);
        int height = Math.max((int)((endFrac - startFrac) * slotHeight), minBlockHeight);
        int x = GRID_MARGIN + 40 + dayIdx * slotWidth;
        Color color = courseColors.getOrDefault(entry.getCourse(), Color.LIGHT_GRAY);
        g2.setColor(color);
        g2.fillRoundRect(x+4, y+4, slotWidth-8, height-8, 10, 10);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(x+4, y+4, slotWidth-8, height-8, 10, 10);
        // Draw label with padding and ellipsis if too long
        String label = entry.getCourse() != null ? entry.getCourse().getCourseName() : "";
        label += " (" + entry.getRoom().getRoomName() + ")";
        FontMetrics fm = g2.getFontMetrics();
        int maxWidth = slotWidth - 16;
        String displayLabel = label;
        if (fm.stringWidth(label) > maxWidth) {
            String ellipsis = "...";
            int len = label.length();
            while (len > 0 && fm.stringWidth(label.substring(0, len) + ellipsis) > maxWidth) {
                len--;
            }
            displayLabel = label.substring(0, len) + ellipsis;
        }
        g2.setColor(Color.BLACK);
        g2.drawString(displayLabel, x+12, y+24);
    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent event) {
        if (entries == null) return null;
        int mx = event.getX();
        int my = event.getY();
        for (ScheduleEntry entry : entries) {
            LocalDateTime start = entry.getStartDateTime();
            LocalDateTime end = entry.getEndDateTime();
            DayOfWeek day = start.getDayOfWeek();
            int dayIdx = day.getValue() - 1;
            if (dayIdx < 0 || dayIdx >= DAYS.length) continue;
            int y = GRID_MARGIN + (int)((start.getHour() + start.getMinute()/60.0 - START_HOUR) * SLOT_HEIGHT);
            int height = (int)(((end.toLocalTime().toSecondOfDay() - start.toLocalTime().toSecondOfDay()) / 3600.0) * SLOT_HEIGHT);
            int x = GRID_MARGIN + 40 + dayIdx * SLOT_WIDTH;
            if (mx >= x+4 && mx <= x+SLOT_WIDTH-4 && my >= y+4 && my <= y+height-4) {
                return String.format("%s\n%s\n%s - %s\nRoom: %s", entry.getCourse().getCourseName(), day, start.toLocalTime(), end.toLocalTime(), entry.getRoom().getRoomName());
            }
        }
        return null;
    }
} 