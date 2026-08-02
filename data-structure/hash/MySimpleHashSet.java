package hash;

import list.linked.MyLinkedList;

public class MySimpleHashSet {
    
    private static final int CAPACITY = 10;
    private MyLinkedList[] buckets;
    
    public MySimpleHashSet() {
        buckets = new MyLinkedList[CAPACITY];
        for (int i = 0; i < CAPACITY; i++) {
            buckets[i] = new MyLinkedList();
        }
    }

    private int getIndex(Object value) {
        int hash = value.hashCode();
        return Math.abs(hash % CAPACITY);
    }

    public void add(Object value) {
        int index = getIndex(value);
        if (!contains(value)) {
            buckets[index].add(value);
        }
    }

    public boolean contains(Object value) {
        int index = getIndex(value);
        return buckets[index].contains(value);
    }

    public static void main(String[] args) {
        MySimpleHashSet set = new MySimpleHashSet();
        set.add("apple");
        set.add("banana");
        set.add("apple");

        System.out.println(set.contains("apple"));
        System.out.println(set.contains("cherry"));
    }

}
