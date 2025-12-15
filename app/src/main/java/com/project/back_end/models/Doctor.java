package com.project.back_end.models;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.AssertTrue;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String name;

    @NotBlank
    @Size(max = 100)
    @Column(name = "specialty", nullable = false, length = 100)
    private String specialty;

    @ElementCollection
    @CollectionTable(name = "doctor_availability", joinColumns = @JoinColumn(name = "doctor_id"))
    private List<TimeSlot> availableTimes = new ArrayList<>();

    public Doctor() {}

    public Doctor(Long id, String name, String specialty) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public List<TimeSlot> getAvailableTimes() { return availableTimes; }
    public void setAvailableTimes(List<TimeSlot> availableTimes) { this.availableTimes = availableTimes; }

    @Embeddable
    public static class TimeSlot {
        @NotNull
        @Enumerated(EnumType.STRING)
        @Column(name = "weekday", nullable = false, length = 10)
        private DayOfWeek weekday;

        @NotNull
        @Column(name = "start_time", nullable = false)
        private LocalTime startTime;

        @NotNull
        @Column(name = "end_time", nullable = false)
        private LocalTime endTime;

        public TimeSlot() {}

        public TimeSlot(DayOfWeek weekday, LocalTime startTime, LocalTime endTime) {
            this.weekday = weekday;
            this.startTime = startTime;
            this.endTime = endTime;
        }

               public DayOfWeek getWeekday() { return weekday; }
        public void setWeekday(DayOfWeek weekday) { this.weekday = weekday; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        @AssertTrue(message = "endTime must be after startTime")
        public boolean isValidTimeRange() {
            if (startTime == null || endTime == null) return true; // NotNull handles null
            return endTime.isAfter(startTime);
        }
    }
}
