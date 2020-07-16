package spring.entity

data class Foo(val name: String, val description: String) {
    constructor() : this("default", "default")

    companion object {
        val invalid = Foo("invalid", "invalid")
    }
}