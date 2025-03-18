package ch.epfl.rechor;

import ch.epfl.rechor.journey.PackedCriteria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MyPackedCriteriaTest {
    @Test
    void testPack() {
        int arrMins = 1000;   // Temps d'arrivée en minutes
        int changes = 5;      // Nombre de changements
        int payload = 42;     // Valeur de la charge utile

        long packed = PackedCriteria.pack(arrMins, changes, payload);

        // Vérifie que les critères sont correctement empaquetés
        assertEquals(arrMins + 240, (packed >>> 39) & ((1L << 12) - 1));
        assertEquals(changes, (packed >>> 32) & ((1L << 7) - 1));
        assertEquals(payload, (int) packed);
    }


    @Test
    void testHasDepMins() {
        long criteriaWithDepMins = PackedCriteria.withDepMins(0L, 500);  // Ajouter un temps de départ
        long criteriaWithoutDepMins = PackedCriteria.withoutDepMins(criteriaWithDepMins);

        assertTrue(PackedCriteria.hasDepMins(criteriaWithDepMins));  // Devrait être vrai
        assertFalse(PackedCriteria.hasDepMins(criteriaWithoutDepMins));  // Devrait être faux
    }

    @Test
    void testDepMins() {
        long criteria = PackedCriteria.withDepMins(0L, 500);  // Ajouter un temps de départ

        assertEquals(500, PackedCriteria.depMins(criteria));  // Vérifie que la valeur récupérée est correcte
    }

    @Test
    void testArrMins() {
        long packed = PackedCriteria.pack(1000, 5, 42);  // Créer des critères

        assertEquals(1000, PackedCriteria.arrMins(packed));  // Vérifie que les minutes d'arrivée sont correctes
    }

    @Test
    void testChanges() {
        long packed = PackedCriteria.pack(1000, 5, 42);  // Créer des critères

        assertEquals(5, PackedCriteria.changes(packed));  // Vérifie que le nombre de changements est correct
    }

    @Test
    void testPayload() {
        long packed = PackedCriteria.pack(1000, 5, 42);  // Créer des critères

        assertEquals(42, PackedCriteria.payload(packed));  // Vérifie que le payload est correct
    }

    @Test
    void testDominatesOrIsEqual() {
        long criteria1 = PackedCriteria.pack(1000, 4, 43);
        long criteria2 = PackedCriteria.pack(1000, 5, 43);

        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));  // criteria1 doit dominer criteria2

        PackedCriteria.withDepMins(criteria1,350);
        PackedCriteria.withDepMins(criteria2, 850);
        assertTrue(PackedCriteria.dominatesOrIsEqual(criteria1, criteria2));
    }

    @Test
    void testWithoutDepMins() {
        long criteriaWithDepMins = PackedCriteria.withDepMins(0L, 500);  // Ajouter un temps de départ
        long criteriaWithoutDepMins = PackedCriteria.withoutDepMins(criteriaWithDepMins);

        assertFalse(PackedCriteria.hasDepMins(criteriaWithoutDepMins));  // Vérifie que les minutes de départ ont été supprimées
    }

    @Test
    void testWithDepMins() {
        long criteria = 0L;
        long criteriaWithDepMins = PackedCriteria.withDepMins(criteria, 500);

        assertTrue(PackedCriteria.hasDepMins(criteriaWithDepMins));  // Vérifie que les minutes de départ sont présentes
        assertEquals(500, PackedCriteria.depMins(criteriaWithDepMins));  // Vérifie que la valeur des minutes est correcte
    }

    @Test
    void testWithAdditionalChange() {
        long criteria = PackedCriteria.pack(1000, 5, 42);
        long updatedCriteria = PackedCriteria.withAdditionalChange(criteria);

        assertEquals(6, PackedCriteria.changes(updatedCriteria));  // Vérifie que le nombre de changements a été incrémenté
    }

    @Test
    void testWithPayload() {
        long criteria = PackedCriteria.pack(1000, 5, 42);
        long updatedCriteria = PackedCriteria.withPayload(criteria, 100);

        assertEquals(100, PackedCriteria.payload(updatedCriteria));  // Vérifie que le payload a été mis à jour
    }






}
