package org.aldogioia.reportingad.model.data;

import lombok.Data;

import java.time.LocalTime;

@Data
public class TimeRange {
    private LocalTime start;
    private LocalTime end;

    public TimeRange(LocalTime start, LocalTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("L'orario di inizio non può essere dopo quello di fine.");
        }
        this.start = start;
        this.end = end;
    }
}

