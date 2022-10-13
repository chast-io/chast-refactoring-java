package analyzers

abstract class Analyzer {
    abstract fun name(): String
    abstract fun fullyQualifiedName(): String
}
