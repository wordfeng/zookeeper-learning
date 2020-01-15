# zookeeper-learning

学习zookeeper写的代码, 由于是自己创建的多项目maven中的一个，所以有需要运行的自行添加以下依赖（我学习时使用的版本）

```xml
<!--  raw zk client      -->
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <version>3.5.6</version>
</dependency>
<!--  curator zk client -->
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>4.2.0</version>
</dependency>

<!-- Java 8 Async DSL -->
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-x-async</artifactId>
    <version>4.2.0</version>
</dependency>

<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-test</artifactId>
    <version>4.2.0</version>
    <scope>test</scope>
</dependency>

<dependency>
     <groupId>log4j</groupId>
     <artifactId>log4j</artifactId>
     <version>1.2.17</version>
</dependency>
<dependency>
    <groupId>commons-logging</groupId>
    <artifactId>commons-logging</artifactId>
    <version>1.2</version>
</dependency>
```

