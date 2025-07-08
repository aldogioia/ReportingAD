package org.aldogioia.reportingad.model.data;


import java.time.LocalTime;

public record TimeRange(LocalTime start, LocalTime end) {
    public TimeRange {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("L'orario di inizio non può essere dopo quello di fine.");
        }
    }
}

