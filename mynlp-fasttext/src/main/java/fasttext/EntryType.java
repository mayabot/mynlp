

package fasttext;

public enum EntryType {

    word(0), label(1);

    public int value;

    EntryType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    static EntryType[] types = EntryType.values();

    public static EntryType fromValue(int value) throws IllegalArgumentException {
        try {
            return types[value];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Unknown EntryType enum second :" + value);
        }
    }

    @Override
    public String toString() {
        return value == 0 ? "word" : value == 1 ? "label" : "unknown";
    }
}