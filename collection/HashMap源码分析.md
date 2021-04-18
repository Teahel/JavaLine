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
    
```
