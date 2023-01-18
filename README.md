# [CHAST] Refactoring Java

Refactorings designed for Java.

## Structure

* `base`: Base operations for running refactorings
  * `analyzers`: Code for analyzing code based on rooster/jdt
  * `roaster_jdt`: Code based on roaster/jdt.
  * `spoon/spoonExtensions`: Code based on spoon.
  * This is also the entry point for loading code and verifying syntax.
* `refactorings`: Home of all the refactorings currently implemented
  * `class_to_record`: Refactoring for converting a java class to a record if possible.
    * A chast recipe is available with some tests available. Check the [main repository](https://github.com/chast-io/chast-core) for a demo.
