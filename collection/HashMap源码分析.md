# HashMap
## HashMap 介绍 
基于 JDK1.8  学习分析

HashMap 主要用来存放键值对,基于哈希表的 Map 接口实现
JDk 1.8以前使用的是数组+链表，数组是主体，链表是为了解决哈希冲突。
JDK 1.8多了红黑树，链表长度出现大于或者等于阈值（TREEIFY_THRESHOLD:8），然后比对数组长度，当大于或者等于（MIN_TREEIFY_CAPACITY:64）链表是转成红黑树，否则进行扩容（resize）

