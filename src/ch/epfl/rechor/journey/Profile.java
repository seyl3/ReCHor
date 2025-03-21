package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record Profile(TimeTable timeTable, LocalDate date, int arrStationId, List<ParetoFront> stationFront) {

    public Profile{
        stationFront = List.copyOf(stationFront);
    }
    public Connections connections(){
        return  timeTable.connectionsFor(date);
    }

    public Trips trips(){
        return timeTable.tripsFor(date);
    }

    public ParetoFront forStation(int stationId){
        if(stationId>=stationFront.size()){
            throw new IndexOutOfBoundsException();
        }
        return stationFront.get(stationId);
    }


    public final static class Builder{
        TimeTable timeTable;
        LocalDate date;
        int arrStationId;
        ParetoFront.Builder[] stationFrontBuilders;
        ParetoFront.Builder[] tripsFrontBuilders;

        public Builder(TimeTable timeTable, LocalDate date, int arrStationId){
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            stationFrontBuilders = new ParetoFront.Builder[timeTable.stations().size()];
            tripsFrontBuilders = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
        }

        public ParetoFront.Builder forStation(int stationId){
            if (stationId<0 || stationId>=stationFrontBuilders.length){
                throw new IndexOutOfBoundsException();
            }
            return stationFrontBuilders[stationId];
        }

        public void setForStation(int stationId, ParetoFront.Builder builder){
            stationFrontBuilders[stationId] = builder;
        }

        public ParetoFront.Builder forTrip(int tripId){
            if (tripId<0 || tripId>=stationFrontBuilders.length){
                throw new IndexOutOfBoundsException();
            }
            return stationFrontBuilders[tripId];
        }

        public void setForTrip(int tripId, ParetoFront.Builder builder){
            tripsFrontBuilders[tripId] = builder;

        }

        public Profile build(){
            List<ParetoFront> stationFront = new ArrayList<>();
            for(ParetoFront.Builder builder : stationFrontBuilders){
                if(builder!=null){
                    stationFront.add(builder.build());
                }else{
                    stationFront.add(ParetoFront.EMPTY);
                }
            }
            return new Profile(timeTable, date, arrStationId, stationFront );
        }

    }

}
