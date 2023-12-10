package example

import com.github.event.Example

interface Service {
  fun handle(message: String)
  fun process(message: Example): Example
}
