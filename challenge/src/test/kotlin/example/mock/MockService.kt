package example.mock

import example.Service

class MockService : Service {

  var receivedRequest = "not recorded"
  override fun handle(message: String) {
    receivedRequest = message
  }
}