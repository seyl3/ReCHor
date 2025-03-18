package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

import static ch.epfl.rechor.journey.PackedCriteria.*;
import static org.junit.jupiter.api.Assertions.*;

class MyParetoFrontTest {

    @Test
    void basicTest() {

        ParetoFront.Builder front = new ParetoFront.Builder();
        front.add(pack(500, 4, 0));
        front.add(pack(600, 3, 0));
        front.add(pack(700, 2, 0));
        front.add(pack(800, 1, 0));
        front.add(pack(900, 0, 0));



        ParetoFront paretoFront = front.build();

        System.out.println(paretoFront.toString());

    }

    @Test
    void emptyParetoFrontHasZeroSize() {
        ParetoFront front = ParetoFront.EMPTY;
        assertEquals(0, front.size(), "An empty ParetoFront should have size 0");
    }

    @Test
    void getThrowsExceptionWhenElementNotFound() {
        ParetoFront front = ParetoFront.EMPTY;
        assertThrows(NoSuchElementException.class, () -> front.get(500, 2), "Fetching a non-existing element should throw an exception");
    }

    @Test
    void builderCreatesEmptyParetoFront() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        assertTrue(builder.isEmpty(), "A new builder should start with an empty ParetoFront");
    }

    @Test
    void builderClearsParetoFront() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(400, 2, 0));
        assertFalse(builder.isEmpty(), "After adding an element, the builder should not be empty");
        builder.clear();
        assertTrue(builder.isEmpty(), "After calling clear, the builder should be empty");
    }

    @Test
    void addingDominatedElementDoesNotChangeFront() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long optimal = pack(500, 1, 0);
        long dominated = pack(550, 2, 0);
        long dominated2 = pack(550, 3, 0);
        builder.add(optimal);
        builder.add(dominated);
        builder.add(dominated2);

        ParetoFront front = builder.build();
        assertEquals(1, front.size(), "The dominated element should not be added to the Pareto front");
        assertEquals(optimal, front.get(500, 1), "The optimal element should be present");
    }

    @Test
    void addingNonDominatedElementIncreasesSize() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        long first = pack(500, 1, 0);
        long second = pack(550, 0, 0);
        builder.add(first);
        builder.add(second);

        ParetoFront front = builder.build();
        assertEquals(2, front.size(), "Both non-dominated elements should be in the Pareto front");
    }

    @Test
    void builderHandlesMultipleDominatedElementsEfficiently() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(500, 3, 0));
        builder.add(pack(520, 2, 0));// Should be ignored (dominated)
        builder.add(pack(540, 1, 0));// Should be ignored (dominated)
        builder.add(pack(560, 0, 0));// Should be ignored (dominated)

        builder.add(pack(510, 2, 0));
        builder.add(pack(530, 1, 0));
        builder.add(pack(550, 0, 0));
        builder.add(pack(400, 4, 0));
        builder.add(pack(300, 5, 0));

        ParetoFront front = builder.build();
        System.out.println(front.toString());
        assertEquals(6, front.size(), "Only the non-dominated elements should remain in the Pareto front");
    }

    @Test
    void forEachIteratesOverAllElements() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(500, 1, 0));
        builder.add(pack(550, 0, 0));

        ParetoFront front = builder.build();
        int[] count = {0};
        front.forEach((LongConsumer) l -> count[0]++);

        assertEquals(2, count[0], "forEach should iterate over all elements in the Pareto front");
    }

    @Test
    void testFullyDominates_ValidDomination() {
        ParetoFront.Builder front1 = new ParetoFront.Builder();
        front1.add(withDepMins(PackedCriteria.pack(600, 2, 0),500));
        front1.add(withDepMins(PackedCriteria.pack(650, 1, 0),500));


        ParetoFront.Builder front2 = new ParetoFront.Builder();
        front2.add(PackedCriteria.pack(620, 3, 0));
        front2.add(PackedCriteria.pack(630, 2, 0));

        assertTrue(front1.fullyDominates(front2, 500),
                "Expected front1 to fully dominate front2.");
    }

    @Test
    void testFullyDominates_NotDominating() {
        ParetoFront.Builder front1 = new ParetoFront.Builder();
        front1.add(withDepMins(PackedCriteria.pack(600, 2, 0),500));
        front1.add(withDepMins(PackedCriteria.pack(650, 1, 0),500));

        ParetoFront.Builder front2 = new ParetoFront.Builder();
        front2.add(PackedCriteria.pack(620, 3, 0));
        front2.add(PackedCriteria.pack(630, 2, 0));
        front2.add(PackedCriteria.pack(590, 1, 0));  // Not dominated

        assertFalse(front1.fullyDominates(front2, 500),
                "Expected front1 to NOT fully dominate front2.");
    }

    @Test
    void testFullyDominates_IdenticalFrontiers() {
        ParetoFront.Builder front1 = new ParetoFront.Builder();
        front1.add(withDepMins(PackedCriteria.pack(600, 2, 0),500));
        front1.add(withDepMins(PackedCriteria.pack(650, 1, 0),500));


        ParetoFront.Builder front2 = new ParetoFront.Builder();
        front2.add(PackedCriteria.pack(600, 2, 0));
        front2.add(PackedCriteria.pack(650, 1, 0));

        assertTrue(front1.fullyDominates(front2, 500),
                "Expected identical frontiers to fully dominate each other.");
    }

    // --- BASIC FUNCTIONALITY TESTS ---

    @Test
    void testEmptyParetoFront() {
        ParetoFront emptyFront = ParetoFront.EMPTY;
        assertEquals(0, emptyFront.size(), "Empty frontier should have size 0.");
    }

    @Test
    void testBuilderInitialEmpty() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        assertTrue(builder.isEmpty(), "Newly created builder should be empty.");
    }

    @Test
    void testBasicAddition() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(600, 2, 0));
        ParetoFront front = builder.build();
        assertEquals(1, front.size(), "Frontier should contain one tuple after adding one.");
    }

    @Test
    void testMultipleAdditions() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(600, 2, 0));
        builder.add(pack(650, 1, 0));
        ParetoFront front = builder.build();
        assertEquals(2, front.size(), "Frontier should contain two tuples after adding two.");
    }

    @Test
    void testOrderingLexicographic() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(600, 2, 0));
        builder.add(pack(550, 3, 0)); // Earlier arrival, should be first

        ParetoFront front = builder.build();
        assertTrue(front.get(550, 3) < front.get(600, 2), "Entries should be ordered by arrival time.");
    }

    // --- TESTING DOMINATION LOGIC ---

    @Test
    void testAddingDominatedTupleIgnored() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(600, 2, 0));
        builder.add(pack(600, 3, 0)); // This is worse (more changes), should be ignored

        ParetoFront front = builder.build();
        assertEquals(1, front.size(), "Dominated tuple should not be added.");
    }

    @Test
    void testAddingTupleThatDominatesExisting() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(600, 2, 0));
        builder.add(pack(600, 1, 0)); // Fewer changes, should replace

        ParetoFront front = builder.build();
        assertEquals(1, front.size(), "Tuple should be replaced when dominated.");
        assertDoesNotThrow(() -> front.get(600, 1), "Only the non-dominated tuple should remain.");
    }

    // --- EXCEPTION HANDLING TESTS ---

    @Test
    void testGetThrowsForNonExistentTuple() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(600, 2, 0));
        ParetoFront front = builder.build();

        assertThrows(NoSuchElementException.class, () -> front.get(500, 2),
                "Requesting a non-existent tuple should throw an exception.");
    }

    // --- FULLY DOMINATES TESTING ---

    @Test
    void testFullyDominatesIdenticalFrontiers() {
        ParetoFront.Builder front1 = new ParetoFront.Builder();
        front1.add(withDepMins(PackedCriteria.pack(600, 2, 0),500));
        front1.add(withDepMins(PackedCriteria.pack(650, 1, 0),500));

        ParetoFront.Builder front2 = new ParetoFront.Builder();
        front2.add(pack(600, 2, 0));
        front2.add(pack(650, 1, 0));

        assertTrue(front1.fullyDominates(front2, 500),
                "Identical frontiers should fully dominate each other.");
    }

    @Test
    void testFullyDominatesWithWeakerFrontier() {
        ParetoFront.Builder strong = new ParetoFront.Builder();
        strong.add(withDepMins(pack(600, 1, 0),500)); // Less changes

        ParetoFront.Builder weak = new ParetoFront.Builder();
        weak.add(pack(600, 2, 0)); // More changes

        assertTrue(strong.fullyDominates(weak, 500),
                "A frontier with strictly better elements should fully dominate a weaker one.");
    }

    @Test
    void testFullyDominatesFailsWithBetterElement() {
        ParetoFront.Builder strong = new ParetoFront.Builder();
        strong.add(withDepMins(pack(600, 2, 0),500));

        ParetoFront.Builder weak = new ParetoFront.Builder();
        weak.add(pack(600, 2, 0));
        weak.add(pack(550, 1, 0)); // This is better

        assertFalse(strong.fullyDominates(weak, 500),
                "A frontier containing a better tuple should not be fully dominated.");
    }

    // --- COMPACT FUNCTION TESTS ---

    @Test
    void testCompactRemovesDominatedElements() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(600, 2, 0));
        builder.add(pack(600, 1, 0)); // Dominates the previous one

        assertEquals(1, builder.build().size(), "Compact should remove dominated elements.");
    }

    @Test
    void testCompactDoesNotRemoveIndependentElements() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(pack(600, 2, 0));
        builder.add(pack(650, 1, 0)); // Not dominated

        assertEquals(2, builder.build().size(), "Independent elements should not be removed.");
    }

    // --- STRESS TESTS ---

    @Test
    void testLargeNumberOfElements() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        for (int i = 0; i < 128; i++) {
            builder.add(pack(600 - i, i, 0));
        }
        ParetoFront front = builder.build();
        assertEquals(128, front.size(), "Should handle large numbers of elements.");
    }
}