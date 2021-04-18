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
     * threshols >= capacity * load_factor,临界值超过之后会扩容
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



