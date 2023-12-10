package example.mock

import com.github.event.Example
import example.Service

class MockService : Service {

  var receivedRequest = "not recorded"
  override fun handle(message: String) {
    receivedRequest = message
  }

  override fun process(message: Example): Example {
    return message
  }
}
