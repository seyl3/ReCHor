package ch.epfl.rechor;

/**
 * Utilitaire pour vérifier des conditions qui doivent être respectées.
 * <p>
 * Cette classe fournit des méthodes statiques pour valider des préconditions
 * dans le code. Si une condition n'est pas respectée, une exception est levée
 * pour signaler l'erreur au plus tôt.
 * </p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public final class Preconditions {
    // Empêche l'instanciation
    private Preconditions() {
    }

    /**
     * Vérifie qu'une condition est vraie.
     *
     * @throws IllegalArgumentException si la condition est fausse
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
