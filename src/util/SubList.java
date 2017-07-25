package util;

import java.lang.Math;
import java.lang.Iterable;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class SubList<T> implements Iterator<List<T>>, Iterable<List<T>> {
    private int size;
    private int groups;
    private int fromIndex;

    private List<T> list;

    public SubList(List<T> list, int groups) {
        this.list = list;
        this.groups = Math.min(groups, list.size());
        size = (int)Math.round((double)list.size() / (double)groups);
        fromIndex = 0;
    }

    public Iterator<List<T>> iterator() {
        return this;
    }

    public boolean hasNext() {
        return groups > 0 && fromIndex < list.size();
    }

    public List<T> next() {
        groups--;

        int to = fromIndex + size;
        int toIndex = (to > list.size() || groups == 0) ? list.size() : to;
        List<T> sublist = list.subList(fromIndex, toIndex);

        fromIndex = to;

        return sublist;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
