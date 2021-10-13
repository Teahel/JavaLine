### ConcurrentHashMap jdk1.8

#### 结构图


![ConcurrentHashMap](https://github.com/Teahel/JavaLine/blob/main/image/ConcurrentHashMap2.png)

节点元素修饰使用了关键词 **final** **volatile**

```
static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        volatile V val;
        volatile Node<K,V> next;

        Node(int hash, K key, V val, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }

        public final K getKey()       { return key; }
        public final V getValue()     { return val; }
        public final int hashCode()   { return key.hashCode() ^ val.hashCode(); }
        public final String toString(){ return key + "=" + val; }
        public final V setValue(V value) {
            throw new UnsupportedOperationException();
        }

```

####  put() 方法

```
final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) throw new NullPointerException();
        // 获取哈希值
        int hash = spread(key.hashCode());
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
                //该哈希值所在下标为空数值时
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                // 使用乐观锁 cas 存入数值
                if (casTabAt(tab, i, null,
                             new Node<K,V>(hash, key, value, null)))
                    break;                   // no lock when adding to empty bin
            }
            // 哈希值（上方方法中被赋值）为 -1时，扩容。
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;
                //使用了 锁 synchronized 同步锁
                synchronized (f) {
                    // 确定该数据下标数值存在
                    if (tabAt(tab, i) == f) {
                        //说明是链表（若为链表或者红黑树，则当前哈希值对应下标下的节点的哈希值为负）
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    break;
                                }
                            }
                        }
                        //如果已是红黑树，则按照红黑树添加
                        else if (f instanceof TreeBin) {
                            Node<K,V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                if (binCount != 0) {
                // 链表节点大于或者等于8，由链表转变为红黑树
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount);
        return null;
    }
```
* 1.初始化数组
* 2.判断当前位置是否为空，如果为空则使用乐观锁（cas）添加数值
* 3.如果当前位置哈希值为-1，则需要扩容
* 4.synchronized锁住当前节点，按照当前为链表或者红黑树方式添加数据
* 5.如果当前为链表，在节点大于或者等于8是转成红黑树
* 
#### get()方法

```
    public V get(Object key) {
        Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
        int h = spread(key.hashCode());
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (e = tabAt(tab, (n - 1) & h)) != null) {
            //获取该哈希值下节点，并匹对哈希值和该节点下的key与传入进来的key是否相等，相等则直接返回value
            if ((eh = e.hash) == h) {
                if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                    return e.val;
            }
            // 该节点的哈希值为负，则说明是链表或者是红黑树。循环判断哈希值和key，最终返回数值
            else if (eh < 0)
                return (p = e.find(h, key)) != null ? p.val : null;
            while ((e = e.next) != null) {
                if (e.hash == h &&
                    ((ek = e.key) == key || (ek != null && key.equals(ek))))
                    return e.val;
            }
        }
        return null;
    }
```
