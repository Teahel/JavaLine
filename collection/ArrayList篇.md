### ArrayList 

#### ArrayList默认初始化大小为10， 实现于 List、RandomAccess 接口。可以插入空数据，也支持随机访问。


* **elementData**  **size** 两个比较重要的元素
   - elementData 保存ArrayList数据的数组
   - size 表示ArrayList 所包含的元素个数

* 添加元素的方法 **add(e)**

```
  public boolean add(E e) {
        //扩容
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        //尾部位置存放
        elementData[size++] = e;
        return true;
    }
```
  - 先扩容，然后**size+1**
  - 在尾部添加元素


* 添加指定位置元素 **add(int index, E element)**

```
   public void add(int index, E element) {
        //判断index是否合法
        rangeCheckForAdd(index);
        //扩容
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        //挪移数据（native方法没办法看）
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        //指定位置加1，                 
        elementData[index] = element;
        //size加1
        size++;
    }
```

* 扩容代码部分

```
    private void grow(int minCapacity) {

        // 将当前数组大小赋值oldCapacity，oldCapacity为旧容量
        int oldCapacity = elementData.length;
        // 当前数组大小右移一位，并加上自身(将oldCapacity 右移一位，其效果相当于oldCapacity /2)
        // newCapacity变成了原来的1.5倍，newCapacity为新容量
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        // 如果新容量还小于最小需求量，那么最新容量以最小需求量为准。
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        // 如果最新需求量比ArrayList定义的容量最大值还大，调用**hugeCapacity**方法,判断最小需求量比AX_ARRAY_SIZE大(值大小为Integer.MAX_VALUE - 8) 则最新量为Integer.MAX_VALUE，不然那为MAX_ARRAY_SIZE。  
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        //数组复制
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
    
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
```
* 1.将当前数组大小值通过右移，得到1.5倍数值，为暂时最新容量
* 2.先比较最小需要量，小于则最新容量大小为最小需要量
* 3.在比较，如果最新容量比ArrayList定义最大容量大则最新容量为int最大整数
* 4.将数组进行复制

