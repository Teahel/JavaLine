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
  - 先扩容，然后 size+1
  - 在尾部添加 元素


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




