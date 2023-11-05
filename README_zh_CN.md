# `service-spi-java`

[English](./README.md)  | 中文

> 一个基于 `@SPI` 注解的 `Java` Service `SPI` 扩展库
>
> 从 [the-way-to-java](https://github.com/photowey/the-way-to-java)
> 分离出来,采用新的包名 `io.github.photowey` 替换原先的 `com.photowey`
> 以后全部启用新的包名 `io.github.photowey`

## 实例:

`Service`

> `TestSPI`
>
> |- 抽象服务
>
> > 继承 `InitializeLifeCycle`
> >
> > > 可以实现一些生命周期方法
> > >
> > > - `start`
        > > >
- 默认方法
> > > - `init`
> > > - `stop`
        > > >
- 默认方法

```java
public interface TestSPI extends InitializeLifeCycle {

    String sayHello();
}
```

### `DefaultTestSPI`

> `DefaultTestSPI`
>
> |- 默认实现

```java

@SPI(value = "default")
public class DefaultTestSPI implements TestSPI {

    @Override
    public String sayHello() {
        //  TODO
    }

    @Override
    public void init() {
        //  TODO
    }
}
```

### `HelloWorldTestSPI`

> `HelloWorldTestSPI`
>
> |- 特定实现

```java

@SPI(value = "helloworld")
public class HelloWorldTestSPI implements TestSPI {

    @Override
    public String sayHello() {
        //  TODO
    }

    @Override
    public void init() {
        //  TODO
    }
}
```