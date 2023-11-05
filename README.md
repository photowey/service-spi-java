# `service-spi-java`

English | [中文](./README_zh_CN.md)

> A `Java` Service `SPI` extension library based on `@SPI` annotation
>
> Separate from [the-way-to-java](https://github.com/photowey/the-way-to-java)
> and use the new package name `io.github.photowey` to replace the
> original `com .photowey`
> Enable the new package name `io.github.photowey` in the future

## `Example`:

`Service`

```java
public interface TestSPI extends InitializeLifeCycle {

    String sayHello();
}
```

### `DefaultTestSPI`

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

