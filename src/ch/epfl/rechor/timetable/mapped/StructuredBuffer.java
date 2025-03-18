package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

import java.nio.ByteBuffer;
import java.util.Objects;

public class StructuredBuffer {
    private final Structure structure;
    private final ByteBuffer buffer;

    public StructuredBuffer(Structure structure, ByteBuffer buffer) {
        Preconditions.checkArgument(buffer.capacity() % structure.totalSize() == 0);
        this.structure = Objects.requireNonNull(structure);
        this.buffer = Objects.requireNonNull(buffer);
    }

    public int size() {
        return buffer.capacity() / structure.totalSize();
    }

    public int getU8(int fieldIndex, int elementIndex) {
        if (fieldIndex >= structure.totalSize() || elementIndex >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(buffer.get(structure.offset(fieldIndex, elementIndex)));
    }

    public int getU16(int fieldIndex, int elementIndex) {
        if (fieldIndex >= structure.totalSize() || elementIndex >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return Short.toUnsignedInt(buffer.getShort(structure.offset(fieldIndex, elementIndex)));
    }

    public int getS32(int fieldIndex, int elementIndex) {
        if (fieldIndex >= structure.totalSize() || elementIndex >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return buffer.getInt(structure.offset(fieldIndex, elementIndex));
    }


}

