# AsyncUnit

Simple tool to test multi-threaded code. It allows you to assert operations in threads. It will wait for threads to finish and will propagate errors to the main thread. 

Inspiration got from [concurrentunit](https://github.com/jhalterman/concurrentunit). 
Advantages over concurentunit:
* simpler usage
* no restrictions on assertion actions

## Usage
Wrap asynchronous part of the code using `AsyncFlow.prepare` and use `AsyncFlow.await` to block the main thread.
If asynchronous part fails with exception in any thread, the test will also fail. 

```java
@Test
public void shouldReceiveMessageOnServer() throws Throwable {

  server.registerListener(AsyncFlow.prepare(
      message -> {
        // Executed on separate thread
        assertEquals(message.body(), "hello");
      }
  ));
  
  server.send("hello");
  
  // Wait for thread to finish or exit after 2s.
  AsyncFlow.await(2000);
}
```
 
Multiple calls to a server:

```java
@Test
public void shouldReceiveMessageOnServer() throws Throwable {

  server.registerListener(AsyncFlow.prepare(
      message -> {
        // Executed on separate thread
        assertEquals(message.body(), "hello");
      }
  ));
  
  server.send("hello");
  server.send("hello");
  
  // Wait for asnyc part to be executed 2 times or exit after 2s 
  AsyncFlow.await(2000, 2);
}
```



