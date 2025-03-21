package ch.epfl.rechor.journey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JourneyExtractor {

    private JourneyExtractor(){}

    public List<Journey> journeys(Profile profile, int depStationId){
        ParetoFront pf = profile.forStation(depStationId);
        List<Journey> journeys = new ArrayList<>();
        pf.forEach((long criteria) -> {
            List<Journey.Leg> legs = new ArrayList<>();

        });

        return journeys;
    }
}
