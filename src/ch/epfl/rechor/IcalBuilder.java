package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Constructeur d'un fichier iCalendar (format .ics) permettant d'ajouter des événements
 * et de structurer les composants VCALENDAR et VEVENT.
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public final class IcalBuilder {

    private final List<Component> startedComponents = new ArrayList<>();
    private final StringBuilder ical = new StringBuilder();
    private final String CRLF = "\r\n";
    private static final int MAX_LINE_LENGTH = 75;
    private static final int FOLD_LENGTH = 74;

    /**
     * Formate une ligne iCalendar en respectant la limite de longueur et le repliement.
     *
     * @param name  Le nom du champ iCalendar
     * @param value La valeur à formater
     * @return La ligne formatée avec repliement si nécessaire
     */
    private String formatLine(Name name, String value) {
        String line = name + ":" + value;
        if (line.length() <= MAX_LINE_LENGTH) {
            return line + CRLF;
        }

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < line.length(); i += FOLD_LENGTH) {
            if (i > 0) {
                formatted.append(CRLF).append(" ");
            }
            formatted.append(line, i, Math.min(i + FOLD_LENGTH, line.length()));
        }
        return formatted.append(CRLF).toString();
    }

    /**
     * Ajoute une paire composant-valeur au fichier iCalendar.
     * Gère le repliement (folding) des longues lignes conformément à la norme iCalendar.
     *
     * @param name  Le nom du champ iCalendar (ex : SUMMARY, DESCRIPTION).
     * @param value La valeur associée.
     * @return L'instance actuelle de {@code IcalBuilder} pour permettre l'enchaînement des appels.
     * @throws IllegalArgumentException si {@code name} ou {@code value} est null.
     */
    public IcalBuilder add(Name name, String value) {
        Preconditions.checkArgument(value != null && name != null);
        ical.append(formatLine(name, value));
        return this;
    }

    /**
     * Ajoute une date/heure à l'événement iCalendar.
     *
     * @param name     Le champ de l'événement (ex: DTSTART, DTEND).
     * @param dateTime La date et l'heure au format {@code LocalDateTime}.
     * @return L'instance actuelle de {@code IcalBuilder} pour permettre l'enchaînement des appels.
     * @throws IllegalArgumentException si {@code name} ou {@code dateTime} est null.
     */
    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        Preconditions.checkArgument(dateTime != null && name != null);
        return add(name, FormatterFr.formatEventTime(dateTime));
    }

    /**
     * Démarre un nouveau composant iCalendar (ex: VCALENDAR, VEVENT).
     *
     * @param component Le composant à ajouter.
     * @return L'instance actuelle de {@code IcalBuilder} pour permettre l'enchaînement des appels.
     * @throws IllegalArgumentException si {@code component} est null.
     */
    public IcalBuilder begin(Component component) {
        Preconditions.checkArgument(component != null);
        startedComponents.add(component);
        ical.append(formatLine(Name.BEGIN, component.toString()));
        return this;
    }

    /**
     * Termine le dernier composant ouvert.
     *
     * @return L'instance actuelle de {@code IcalBuilder} pour permettre l'enchaînement des appels.
     * @throws IllegalArgumentException si aucun composant n'est en cours d'écriture.
     */
    public IcalBuilder end() {
        Preconditions.checkArgument(!startedComponents.isEmpty());
        Component lastComponent = startedComponents.getLast();
        ical.append(formatLine(Name.END, lastComponent.toString()));
        startedComponents.removeLast();
        return this;
    }

    /**
     * Construit et retourne la chaîne iCalendar complète.
     *
     * @return La représentation sous forme de chaîne du fichier iCalendar généré.
     * @throws IllegalArgumentException si des composants n'ont pas été fermés correctement ou
     *                                  qu'on essaie de construire un ical vide
     */
    public String build() {
        Preconditions.checkArgument(startedComponents.isEmpty());
        return ical.toString();
    }

    public enum Component {VCALENDAR, VEVENT}

    public enum Name {
        BEGIN,
        END,
        PRODID,
        VERSION,
        UID,
        DTSTAMP,
        DTSTART,
        DTEND,
        SUMMARY,
        DESCRIPTION
    }


}
