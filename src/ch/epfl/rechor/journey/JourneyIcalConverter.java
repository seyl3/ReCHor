package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Convertisseur permettant de générer un fichier iCalendar (.ics) à partir d'un trajet {@code
 * Journey}.
 */
public final class JourneyIcalConverter {
    private JourneyIcalConverter() {
    }

    /**
     * Convertit un trajet {@code Journey} en une représentation iCalendar.
     *
     * @param journey Le trajet à convertir en iCalendar.
     * @return Une chaîne de caractères contenant la représentation iCalendar du trajet.
     */
    public static String toIcalendar(Journey journey) {
        IcalBuilder ical = new IcalBuilder()
                .begin(IcalBuilder.Component.VCALENDAR)
                .add(IcalBuilder.Name.VERSION, "2.0")
                .add(IcalBuilder.Name.PRODID, "ReCHor")
                .begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.UID, UUID.randomUUID().toString())
                .add(IcalBuilder.Name.DTSTAMP, FormatterFr.formatEventTime(LocalDateTime.now()))
                .add(IcalBuilder.Name.DTSTART, FormatterFr.formatEventTime(journey.depTime()))
                .add(IcalBuilder.Name.DTEND, FormatterFr.formatEventTime(journey.arrTime()))
                .add(IcalBuilder.Name.SUMMARY,
                        journey.depStop().name() + " → " + journey.arrStop().name());

        StringBuilder description = new StringBuilder();
        for (int i = 0; i < journey.legs().size(); i++) {
            switch (journey.legs().get(i)) {
                case Journey.Leg.Foot f:
                    description.append(FormatterFr.formatLeg(f));
                    if (i < journey.legs().size() - 1) {
                        description.append("\\n");
                    }
                    break;
                case Journey.Leg.Transport t:
                    description.append(FormatterFr.formatLeg(t));
                    if (i < journey.legs().size() - 1) {
                        description.append("\\n");
                    }
                    break;
            }
        }


        ical.add(IcalBuilder.Name.DESCRIPTION, description.toString())
                .end()
                .end();

        return ical.build();


    }

}
