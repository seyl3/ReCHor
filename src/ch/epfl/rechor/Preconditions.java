package ch.epfl.rechor;

public final class Preconditions {
    private Preconditions() {
    }

    /**
     * Vérfie si la condition fournie est respectée.
     *
     * @param shouldBeTrue la condition a vérifier
     * @throws IllegalArgumentException si la condition est false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }


}
