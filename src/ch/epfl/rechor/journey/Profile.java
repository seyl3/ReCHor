package ch.epfl.rechor.journey;

        import ch.epfl.rechor.timetable.Connections;
        import ch.epfl.rechor.timetable.TimeTable;
        import ch.epfl.rechor.timetable.Trips;

        import java.time.LocalDate;
        import java.util.ArrayList;
        import java.util.List;

        /**
         * La classe Profile représente un profil de voyage pour une date et une station d'arrivée données.
         * Elle contient les informations sur les connexions et les trajets pour cette date.
         *
         * @author Sarra Zghal, Elyes Ben Abid
         *
         */
        public record Profile(TimeTable timeTable, LocalDate date, int arrStationId,
                              List<ParetoFront> stationFront) {

            /**
             * Construit une instance de Profile avec les paramètres spécifiés.
             *
             * @param timeTable l'horaire des trajets
             * @param date la date du profil
             * @param arrStationId l'identifiant de la station d'arrivée
             * @param stationFront la liste des frontières de Pareto pour chaque station
             */
            public Profile {
                stationFront = List.copyOf(stationFront);
            }

            public Connections connections() {
                return timeTable.connectionsFor(date);
            }

            public Trips trips() {
                return timeTable.tripsFor(date);
            }

            /**
             * Retourne la frontière de Pareto pour une station donnée.
             *
             * @param stationId l'identifiant de la station
             * @return la frontière de Pareto pour la station donnée
             * @throws IndexOutOfBoundsException si l'identifiant de la station est invalide
             */
            public ParetoFront forStation(int stationId) {
                if (stationId >= stationFront.size()) {
                    throw new IndexOutOfBoundsException();
                }
                return stationFront.get(stationId);
            }

            /**
             * La classe Builder permet de construire une instance de Profile.
             */
            public final static class Builder {
                TimeTable timeTable;
                LocalDate date;
                int arrStationId;
                ParetoFront.Builder[] stationFrontBuilders;
                ParetoFront.Builder[] tripsFrontBuilders;

                public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
                    this.timeTable = timeTable;
                    this.date = date;
                    this.arrStationId = arrStationId;
                    stationFrontBuilders = new ParetoFront.Builder[timeTable.stations().size()];
                    tripsFrontBuilders = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
                }

                /**
                 * Retourne le Builder de la frontière de Pareto pour une station donnée.
                 *
                 * @param stationId l'identifiant de la station
                 * @return le Builder de la frontière de Pareto pour la station donnée
                 * @throws IndexOutOfBoundsException si l'identifiant de la station est invalide
                 */
                public ParetoFront.Builder forStation(int stationId) {
                    if (stationId < 0 || stationId >= stationFrontBuilders.length) {
                        throw new IndexOutOfBoundsException();
                    }
                    return stationFrontBuilders[stationId];
                }

                /**
                 * Définit le Builder de la frontière de Pareto pour une station donnée.
                 *
                 * @param stationId l'identifiant de la station
                 * @param builder le Builder de la frontière de Pareto
                 */
                public void setForStation(int stationId, ParetoFront.Builder builder) {
                    stationFrontBuilders[stationId] = builder;
                }

                /**
                 * Retourne le Builder de la frontière de Pareto pour un trajet donné.
                 *
                 * @param tripId l'identifiant du trajet
                 * @return le Builder de la frontière de Pareto pour le trajet donné
                 * @throws IndexOutOfBoundsException si l'identifiant du trajet est invalide
                 */
                public ParetoFront.Builder forTrip(int tripId) {
                    if (tripId < 0 || tripId >= tripsFrontBuilders.length) {
                        throw new IndexOutOfBoundsException();
                    }
                    return tripsFrontBuilders[tripId];
                }

                /**
                 * Définit le Builder de la frontière de Pareto pour un trajet donné.
                 *
                 * @param tripId l'identifiant du trajet
                 * @param builder le Builder de la frontière de Pareto
                 */
                public void setForTrip(int tripId, ParetoFront.Builder builder) {
                    tripsFrontBuilders[tripId] = builder;
                }

                public Profile build() {
                    List<ParetoFront> stationFront = new ArrayList<>();
                    for (ParetoFront.Builder builder : stationFrontBuilders) {
                        if (builder != null) {
                            stationFront.add(builder.build());
                        } else {
                            stationFront.add(ParetoFront.EMPTY);
                        }
                    }
                    return new Profile(timeTable, date, arrStationId, stationFront);
                }
            }
        }