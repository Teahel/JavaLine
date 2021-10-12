### Vector
初始容量为10

实际和ArrayList差不多，也一样实现了**list** 和底层数据结构，不过在添加数据的地方使用了**synchronized** 关键字
所以不是并发容器而是同步容器

**add()** 方法
```
   public synchronized boolean add(E e) {
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = e;
        return true;
    }
```

**add(int index, E element)**
```
   public void add(int index, E element) {
        insertElementAt(element, index);
    }
```
```
  public synchronized void insertElementAt(E obj, int index) {
        modCount++;
        if (index > elementCount) {
            throw new ArrayIndexOutOfBoundsException(index
                                                     + " > " + elementCount);
        }
        ensureCapacityHelper(elementCount + 1);
        System.arraycopy(elementData, index, elementData, index + 1, elementCount - index);
        elementData[index] = obj;
        elementCount++;
    }
```
