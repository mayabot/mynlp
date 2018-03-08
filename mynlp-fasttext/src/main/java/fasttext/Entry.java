
package fasttext;

import com.carrotsearch.hppc.IntArrayList;

public class Entry {
    public String word;
    public long count;
    public EntryType type;
    public IntArrayList subwords;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Entry [word=");
        builder.append(word);
        builder.append(", count=");
        builder.append(count);
        builder.append(", type=");
        builder.append(type);
        builder.append(", subwords=");
        builder.append(subwords);
        builder.append("]");
        return builder.toString();
    }

}