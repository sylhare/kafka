package spring.entity

data class Foo(private val name: String, private val description: String) {
    constructor() : this("default", "default")

    companion object {
        val invalid = Foo("invalid", "invalid")
    }
}