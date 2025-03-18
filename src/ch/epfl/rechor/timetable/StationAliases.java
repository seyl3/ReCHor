package ch.epfl.rechor.timetable;

// noms alternatifs des gares
public interface StationAliases extends Indexed {
    // Aussi lèvent des IndexOutOfBound
    String alias(int id);

    // le nom de la gare à laquelle correspond l'alias à l'index donné
    String stationName(int id);


}
