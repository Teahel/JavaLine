# HashMap
## HashMap 介绍 
基于 JDK1.8  学习分析

HashMap 主要用来存放键值对,基于哈希表的 Map 接口实现
JDk 1.8以前使用的是数组+链表，数组是主体，链表是为了解决哈希冲突。
JDK 1.8多了红黑树
链表长度出现大于或者等于阈值（TREEIFY_THRESHOLD:8），然后比对数组长度，当大于或者等于（MIN_TREEIFY_CAPACITY:64）链表是转成红黑树，否则进行扩容（resize）

## 默认初始参数值
```
public class HashMap<K,V> extends AbstractMap<K,V>  implements Map<K,V>, Cloneable, Serializable {
    // 序列号
    private static final long serialVersionUID = 362498820763181265L;
    
    
    /**
     * 默认初始容量 必须是2的幂。
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * 最大容量，可以使用带参数的构造函数指定大小， 必须小于2的30次方
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认负载因子
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 链表长度出现大于或者等于阈值（TREEIFY_THRESHOLD:8），然后比对数组长度，当大于或者等于（MIN_TREEIFY_CAPACITY:64）链表是转成红黑树，否则进行扩容（resize）
     * 
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 红黑树转成链表
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 链表长度出现大于或者等于阈值（TREEIFY_THRESHOLD:8），然后比对数组长度，当大于或者等于（MIN_TREEIFY_CAPACITY:64）链表是转成红黑树，否则进行扩容（resize）
     */
    static final int MIN_TREEIFY_CAPACITY = 64;
    
   // 每次扩容和更改map结构的计数器
    transient int modCount;
    
    /**
     * map中实际存储键值对数据大小(不是数组大小)
     * The number of key-value mappings contained in this map.
     */
    transient int size;
    
    /**
     * threshold >= capacity * load_factor,临界值超过之后会扩容
     */
    int threshold;
    
       // 加载因子
    final float loadFactor;
```

## 默认负载因子(DEFAULT_LOAD_FACTOR)为0.75f

负载因子确定了数组的疏密程度，越接近1越密集，越接近0越疏漏
太疏漏就会导致查找效果太差，太密集导致资源利用率不高
0.75是给出的一个默认值，官方应该是在测试过后觉得这个值比较恰当的。

数组在threshols >= capacity * load_factor,临界值超过之后会扩容

## HashMap 四个构造函数


```
// 指定容量大小和负载因子
 public HashMap(int initialCapacity, float loadFactor) {
        //不允许小于0
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        // 不允许大于1<<30(2的30次方)                                  
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        //tableSizeFor 下面专门解释该方法
        this.threshold = tableSizeFor(initialCapacity);
}
 
 // 指定容量大小
 public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
}

// 无参数，使用容量大小为默认值16 ，负载因子为0.75
public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
}

//传一个map作为参数，负载因子为0.75，容量为足以容纳指定Map中的映射的初始容量
public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
}
    
```

### putMapEntries
```
final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            if (table == null) { // pre-size
                //控制最大值容量不能大于MAXIMUM_CAPACITY
                float ft = ((float)s / loadFactor) + 1.0F;
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
                // 当t>阈值，则初始化阈值，确保为2的幂      
                if (t > threshold)
                    threshold = tableSizeFor(t);
            }
            //当数组大于当前容量*负载因子 进行扩容
            else if (s > threshold)
                resize();
            //转存    
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }
```

### tableSizeFor方法，使得返回值为2幂
```  
   static final int tableSizeFor(int cap) {
        int n = cap - 1;
    （1）步  n |= n >>> 1;
    （2）步  n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
    （3）步   return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }


     n |= n >>> 1;等于 n = n | (n>>>1)
     >>>：右移符号   |：位或 两个位只要有一个为1，那么结果就是1，否则就为0
     如 cap = 11;
     n = 10 
 （1）步 10 二进制 为 00001010
     右移动一位为 00000101
     并进行位或   
     00000101
     00001010
  =  00001111
（2）步 00001111 右移动2位 00000011
       进行位或
       00001111
       00000011
     = 00001111  
 （3）往下以此类推
              n        + 1
  最终得n+1 =8+4+2+1   + 1
  返回值为16
  ```


### putVal方法
```
// put方法
public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
    
hash – hash for key
key – the key
value – the value to put
onlyIfAbsent – if true, 不修改已经保存的值
evict – 如果为false，则表处于创建模式。
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
            // 如果没有则resize()创建
            n = (tab = resize()).length;
            
            // i=(n - 1) & hash 得到节点存储位置，并赋值给p,当p为空时则直接new一个新节点赋值。
        if ((p = tab[i = (n - 1) & hash]) == null)    
            tab[i] = newNode(hash, key, value, null);
        else {
            // 当发现不为空，确哈希值和key相同则直接赋值给e
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            // 发现p是TreeNode的实例，也就是树节点使用putTreeVal方法添加了（红黑树添加节点）
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                /**
                 * p.next为空的时候赋值一个新节点
                 * 当binCount计算是否超过阈值8，当超过阈值时执行treeifyBin方法
                 * treeifyBin方法下面有解析，作用为当发现数组长度小于MIN_TREEIFY_CAPACITY（64）时进行扩容
                 * 否则将链表转换成红黑树
                 */
                for (int binCount = 0; ; ++binCount) {
                    //前面的if ((p = tab[i = (n - 1) & hash]) == null)已经赋值，此时p.next为空，则直接赋值新节点
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        // 判断是否大于阈值8（TREEIFY_THRESHOLD）
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    // p.next不为空，也就是e不为空，且e的哈希值和key与传入值相同(链表为循环链表，所以此时相同时则结束循环)
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key（已经存在数值了）
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null) //onlyIfAbsent为false 直接覆盖（该解释点是描述put方法所以onlyIfAbsent为false），
                    e.value = value;
                //允许 LinkedHashMap后处理的回调    
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        // 实际数据大小是否大于开关，如果是则需要扩容
        if (++size > threshold)
            resize();
        // 插入节点后处理。 具体实现在LinkedHashMap 中。   
        afterNodeInsertion(evict);
        return null;
    }
```

#### resize扩容方法

```
 final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        // 注意threshold = capacity * load_factor
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
             // (MAXIMUM_CAPACITY = 2的30次方)
            if (oldCap >= MAXIMUM_CAPACITY) {
                // Integer.MAX_VALUE =2的31 -1
                threshold = Integer.MAX_VALUE;
                // 修改门限之后直接返回数组
                return oldTab;
            }
            // DEFAULT_INITIAL_CAPACITY = 16， << ：左移，这个判断是超过16，小于最大值之后数组变成扩展成原来的两倍
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // 左移一位变成原来两倍大小门限
        }
        else if (oldThr > 0) // threshold已经复制给oldThr
            newCap = oldThr;
        else {               // 门限为0，则使用默认参数重新初始化。
            newCap = DEFAULT_INITIAL_CAPACITY;  //16
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY); //0.75* 16
        }
        // 如果新门限等于0
        if (newThr == 0) {
            // ft=新门限*默认负载因子
            float ft = (float)newCap * loadFactor;
            // 新的初始化容量小于最大容量值，并且ft小于最大容量值，则新门限为ft,否则为2的31次方减1.
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        // 新门限赋值旧门限
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        // 新生成一个数组
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        // 赋值到全局数据table中
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

```

    
    
    
    
