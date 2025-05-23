package ch.epfl.rechor.journey;

/** Écouteur d’avancement du calcul.
 * 0 ≤ progress ≤ 1 ;  -1 signifie indéterminé ou réinitialisé. */
@FunctionalInterface
public interface ProgressListener {
    void progress(double value);
}