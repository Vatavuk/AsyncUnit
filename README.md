<img src="https://raw.githubusercontent.com/Vatavuk/vgv-parent/master/logo.png" alt="drawing" height="100"/>

# AsyncUnit

[![EO principles respected here](http://www.elegantobjects.org/badge.svg)](http://www.elegantobjects.org)

[![Build Status](https://github.com/Vatavuk/asyncunit/actions/workflows/ci.yml/badge.svg)](https://github.com/Vatavuk/asyncunit/actions/workflows/ci.yml/badge.svg)
[![Test Coverage](https://codecov.io/gh/Vatavuk/asyncunit/branch/master/graph/badge.svg)](https://codecov.io/gh/Vatavuk/asyncunit)
[![Hits-of-Code](https://hitsofcode.com/github/Vatavuk/asyncunit?branch=main)](https://hitsofcode.com/view/github/Vatavuk/asyncunit?branch=main)
[![SonarQube](https://img.shields.io/badge/sonar-ok-green.svg)](https://sonarcloud.io/dashboard/index/hr.com.vgv:asyncunit)

[![Javadocs](http://javadoc.io/badge/hr.com.vgv/asyncunit.svg)](http://javadoc.io/doc/hr.com.vgv/asyncunit)
[![Maven Central](https://img.shields.io/maven-central/v/hr.com.vgv/asyncunit.svg)](https://maven-badges.herokuapp.com/maven-central/hr.com.vgv/asyncunit)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT)

Simple tool for testing multi-threaded code. It allows you to assert operations in threads. It will wait for threads to finish and will propagate errors to the main thread. 

Inspiration got from [concurrentunit](https://github.com/jhalterman/concurrentunit). 
Advantages over concurentunit:
* simpler usage
* no restrictions on assertion actions

## Usage
Wrap asynchronous part of the code using `AsyncFlow.prepare` and use `AsyncFlow.await` to block the main thread.
If asynchronous part fails with exception, the test will also fail. 

```java
@Test
public void doesSomeWorkInSeparateThread() throws Exception {

    new Thread(
        AsyncFlow.prepare(() -> {
            work = doSomeWork();
            assertNotNull(work);
        })
    ).start();
    
    AsyncFlow.await();
}
```
 
Assertions in multiple threads.

```java
@Test
public void doesMultipleWorkInThreads() throws Throwable {

    for (int i = 0; i < 4; i++)
    {
        new Thread(
            AsyncFlow.prepare(() -> {
                work = doSomeWork();
                assertNotNull(work);
            })
        ).start();
    }
    
    AsyncFlow.await(1000, 4);
}
```
`AsyncFlow.await` will work properly only if `AsyncFlow.prepare` is called in the same thread where `await` is triggered.
This will usually be the main test thread. However, if you need flexibility, you can instantiate `AsyncFlow` and prepare flow lazy:
```java
@Test
public void supportsLazyFlowPreparation() throws Exception {

    AsyncFlow.Single flow = new AsyncFlow.Single();

    new Thread(() -> {
        flow.prepare(() -> {
             work = doSomeWork();
             assertNotNull(work);
        }).run();
    }).start();
    
    flow.await();
}
```


