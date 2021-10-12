### LinkedList 

底层是双向链表结构实现，实现了 **List, Deque** 接口

![LinkedList](https://github.com/Teahel/JavaLine/blob/main/image/LinkedList.png)


#### add() 方法

```
public boolean add(E e) {
        linkLast(e);
        return true;
    }
```

```

   /**
     * Links e as last element.
     */
void linkLast(E e) {
     final Node<E> l = last;
     final Node<E> newNode = new Node<>(l, e, null);
     last = newNode;
     if (l == null)
          first = newNode;
      else
       l.next = newNode;
     size++;
       modCount++;
  }
```

#### get(index) 方法
校验index,之后返回数值
```
public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }
    
Node<E> node(int index) {
    // assert isElementIndex(index);
size 右移一位，变成原来一半，然后从中间往前或者往后查询
   if (index < (size >> 1)) {
      Node<E> x = first;
      for (int i = 0; i < index; i++)
           x = x.next;
         return x;
     } else {
        Node<E> x = last;
        for (int i = size - 1; i > index; i--)
           x = x.prev;
        return x;
    }
}
```

#### 总结

1.插入和删除十分便捷
2.遍历查询效率比较低

