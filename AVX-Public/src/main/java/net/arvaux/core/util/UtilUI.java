package net.arvaux.core.util;


import net.arvaux.core.util.sequences.ExplicitSequence;
import net.arvaux.core.util.sequences.Sequence;

/**
 *
 */
public class UtilUI {

    public static int[] getIndicesFor(int items, int startingLine, int newLinePadding) {
        return getIndicesFor(items, startingLine, 5, newLinePadding);
    }

    public static int[] getIndicesFor(int items, int startingLine) {
        return getIndicesFor(items, startingLine, 5, 0);
    }

    public static int[] getIndicesFor(int items, int startingLine, int itemsPerLine, int newLinePadding) {
        itemsPerLine = UtilMath.clamp(itemsPerLine, 1, 9);

        int[] indices = new int[items];

        int lines = (int) Math.ceil(items / ((double) itemsPerLine));
        for (int line = 0; line < lines; line++) {
            int itemsInCurLine = line == lines - 1 ? items - (line * itemsPerLine) : itemsPerLine;
            int startIndex = (startingLine * 9) + ((newLinePadding * 9) * line) + 9 * line - itemsInCurLine + 5;

            for (int item = 0; item < itemsInCurLine; item++) {
                indices[(line * itemsPerLine) + item] = startIndex + (item * 2);
            }
        }

        return indices;
    }

    /**
     * Generates center slots (Padding of 1 from sides of inventory, to allow for panes).
     *
     * @param items The amount of slots required
     * @return The generated slots
     */
    public static int[] getCenterIndices(int items) {
        if (items > 28) {
            throw new IllegalArgumentException("Too many items! Max 28.");
        }

        // Speed up
        if (items == 0) {
            return new int[0];
        }

        int lines = (int) Math.ceil((double) items / 7.0);

        // The amount of slots that will be empty on the last line
        int emptySlots = (lines * 7) - items;

        int[] slots = new int[items];

        int index = 0;
        for (int line = 1; line < lines; line++) {
            int start = (line * 9) + 1;
            for (int column = 0; column < 7 && index < items; column++) {
                slots[index] = start + column;
                index++;
            }
        }

        int requiredSlots = 7 - emptySlots;

        if (requiredSlots <= 5) {
            // If the number is even we do not utilize the center slot, and it will
            // look the best if we space out the rest too
            boolean spacing = requiredSlots % 2 == 0;

            int withSpacing = spacing ? ((2 * requiredSlots) - 1) : requiredSlots;
            int padding = (9 - withSpacing) / 2;
            int slot = (lines * 9) + padding;

            for (int i = 0; i < requiredSlots; i++) {
                slots[index] = slot;
                slot += spacing ? 2 : 1;
                index++;
            }
        } else {
            // Originally this handled lines with 5 members too, but I changed it.
            // This statement still supports 5 member lines too, since there was no
            // benefit to removing it
            int spacesRemoved = requiredSlots - 4;

            boolean doCenter = spacesRemoved != 2;
            // This will never be 1 unless this line has 5 members, but as mentioned
            // earlier support for 5 members is still present
            boolean doSides = spacesRemoved != 1;

            int slot = (lines * 9) + 1;

            // This determines if the spacing after the current slot should be used
            Sequence<Boolean> useSpace = new ExplicitSequence<>(doSides, doCenter, doSides);

            for (int i = 0; i < 4; i++) {
                slots[index] = slot;
                index++;

                if (index >= items) {
                    // Escape if complete. Alternatively we could add a "false" to
                    // the end of the sequence, however this may reduce performance
                    // slightly (Very little, we are talking millis)
                    break;
                }

                slot++;

                if (useSpace.next()) {
                    slots[index] = slot;
                    index++;
                }

                slot++;
            }
        }

        return slots;
    }
}
