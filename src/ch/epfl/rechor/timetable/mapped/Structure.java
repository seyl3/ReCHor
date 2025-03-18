package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

public class Structure {
    private final Field[] fields;
    private final int[] fieldOffsets;
    private final int totalSize;

    public Structure(Field... fields) {
        Preconditions.checkArgument(fields.length != 0 && fields[0].index() == 0);

        for (int i = 1; i < fields.length; i++) {
            Preconditions.checkArgument(fields[i].index == i);
        }
        this.fields = fields.clone();
        this.fieldOffsets = new int[fields.length];

        int offset = 0;
        for (int i = 0; i < fields.length; i++) {
            fieldOffsets[i] = offset;
            offset += fields[i].type().size;
        }
        this.totalSize = offset;
    }

    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

    public int totalSize() {
        return totalSize;
    }

    public int offset(int fieldIndex, int elementIndex) {
        if (fieldIndex < 0 || fieldIndex >= fields.length) {
            throw new IndexOutOfBoundsException();
        }
        if (elementIndex < 0) {
            throw new IndexOutOfBoundsException();
        }
        return elementIndex * totalSize + fieldOffsets[fieldIndex];
    }


    public enum FieldType {
        U8(1), U16(2), S32(4);

        final int size;

        FieldType(int size) {
            this.size = size;
        }

    }

    public record Field(int index, FieldType type) {
        public Field {
            if (type == null) {
                throw new NullPointerException();
            }
        }
    }


}

