package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.TimeTable;

public record Router(TimeTable tt) {

    public Profile profile() {
        return null;
    }
}
