package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.util.ArrayList;


/**
 * Constructeur d'un fichier iCalendar (format .ics) permettant d'ajouter des événements
 * et de structurer les composants VCALENDAR et VEVENT.
 */
public final class IcalBuilder {


    private final ArrayList<Component> startedComponents = new ArrayList<>();
    private final StringBuilder ical = new StringBuilder();
    private final String CRLF = "\r\n";

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

        // Gestion du repliement des longues lignes (max 75 caractères par ligne)
        if (value.length() > 75) {
            value = name + ":" + value;
            StringBuilder newValue = new StringBuilder();

            for (int i = 0; i < value.length(); i += 74) {
                newValue.append(value, i, Math.min(i + 74, value.length()));
                if (i + 74 < value.length()) {
                    newValue.append(CRLF).append(" "); // Ajout d'un espace pour respecter la
                    // norme iCalendar
                }
            }

            ical.append(newValue).append(CRLF);
        } else {
            ical.append(name).append(":").append(value).append(CRLF);
        }

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

        ical.append(name).append(":").append(FormatterFr.formatEventTime(dateTime)).append(CRLF);
        return this;
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
        ical.append(Name.BEGIN).append(':').append(component).append(CRLF);
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
        ical.append(Name.END).append(":").append(lastComponent).append(CRLF);
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
