package list.array;

public class MyArrayList {
    private Object[] elementData;
    private int size;

    public MyArrayList() {
        elementData = new Object[5];
    }

    public void add(Object value) {
        if (size == elementData.length) {
            grow();
        }

        elementData[size] = value;
        size++;
    }

    private void grow() {
        int newCapacity = elementData.length + (elementData.length/2);
        Object[] newArray = new Object[newCapacity];
        System.arraycopy(elementData, 0, newArray, 0, size);
        elementData = newArray;
        System.out.println("Array grown to new capacity: " + newCapacity);
    }

    public static void main(String[] args) {
        MyArrayList list = new MyArrayList();
        for(int i = 0; i < 11; i ++ ) {
            list.add(i);
            System.out.println("Added: " + i);
        }
    }
}