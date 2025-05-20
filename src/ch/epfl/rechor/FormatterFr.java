package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Classe utilitaire pour formater les informations liées aux trajets.
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public final class FormatterFr {
    private static final DateTimeFormatter TIME_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendValue(ChronoField.HOUR_OF_DAY)
                    .appendLiteral('h')
                    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                    .toFormatter();
    private static final DateTimeFormatter EVENT_TIME_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendValue(ChronoField.YEAR)
                    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                    .appendValue(ChronoField.DAY_OF_MONTH, 2)
                    .appendLiteral('T')
                    .appendValue(ChronoField.HOUR_OF_DAY, 2)
                    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                    .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                    .toFormatter();

    private FormatterFr() {
    }

    /**
     * Formate une durée en une chaîne de caractères.
     *
     * @param duration la durée à formater
     * @return la durée formatée sous forme de chaîne
     */
    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        if (hours > 0) {
            return hours + " h " + minutes + " min";
        } else {
            return minutes + " min";
        }
    }

    /**
     * Formate un objet {@code LocalDateTime} en une chaîne représentant l'heure.
     * Construit une instance d'un formatter statique.
     *
     * @param dateTime l'heure à formater
     * @return l'heure formatée sous forme de chaîne de charactère de selon le modèle HHhMM
     */
    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(TIME_FORMATTER);
    }

    /**
     * Formate un objet {@code LocalDateTime} sous le format utilisé par les fichier iCalendar.
     * Construit une instance d'un formatter statique.
     *
     * @param dateTime
     * @return une chaine de charactère de selon le modèle YYYYMMDDTHHMM00
     */
    public static String formatEventTime(LocalDateTime dateTime) {
        return dateTime.format(EVENT_TIME_FORMATTER);
    }

    /**
     * Formate le nom de la voie ou du quai d'un arrêt.
     *
     * @param stop l'arrêt à formater
     * @return le nom formaté de la voie ou du quai, ou une chaîne vide si aucun nom n'est présent
     */
    public static String formatPlatformName(Stop stop) {
        if (stop.platformName() == null || stop.platformName().isEmpty()) {
            return "";
        }
        String platform = stop.platformName();
        String type = Character.isDigit(platform.charAt(0)) ? "voie " : "quai ";

        return type + platform;
    }

    /**
     * Formate une étape à pied.
     *
     * @param footLeg l'étape à pied à formater
     * @return la description formatée de l'étape à pied
     */
    public static String formatLeg(Journey.Leg.Foot footLeg) {
        String type = footLeg.isTransfer() ? "changement" : "trajet à pied";
        return type + " (" + formatDuration(footLeg.duration()) + ")";
    }

    /**
     * Formate une étape de transport.
     *
     * @param leg l'étape de transport à formater
     * @return la description formatée de l'étape de transport
     */
    public static String formatLeg(Journey.Leg.Transport leg) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatTime(leg.depTime())).append(" ")
                .append(leg.depStop().name());

        String depPlatform = formatPlatformName(leg.depStop());

        if (!depPlatform.isEmpty()) {
            sb.append(" (").append(depPlatform).append(")");
        }

        sb.append(" → ").append(leg.arrStop().name()).append(" (arr. ")
                .append(formatTime(leg.arrTime()));

        String arrPlatform = formatPlatformName(leg.arrStop());

        if (!arrPlatform.isEmpty()) {
            sb.append(" ").append(arrPlatform);
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * Formate l'itinéraire et la destination d'une étape de transport.
     *
     * @param transportLeg l'étape de transport à formater
     * @return l'itinéraire et la destination formatés sous forme de chaîne
     */
    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        return transportLeg.route() + " Direction " + transportLeg.destination();
    }


}
