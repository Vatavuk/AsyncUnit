# AsyncUnit

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
`AsyncFlow.prepare` can only work properly if it is called on the same thread where `AsyncFlow.await` is triggered.
This will usually be the main test thread. However, if you need flexibility, you can instantiate `AsyncFlow` and `prepare` flow lazy:
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


