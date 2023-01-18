package analyzers

import spoon.Launcher
import spoon.support.reflect.code.CtFieldReadImpl
import spoon.support.reflect.code.CtReturnImpl
import spoon.support.reflect.declaration.CtClassImpl
import spoon.support.reflect.reference.CtFieldReferenceImpl
import spoon.spoonExtensions.CtClassExtension


fun main() {
//    val source = Roaster.parse(
//        JavaClassSource::class.java,
//        File("/shared/home/rjenni/Projects/mse-repos/master-thesis/MA-Playground/samples/java/records/class/class_before.java")
//    )

//    val unit = Roaster.parseUnit(
//        File("/shared/home/rjenni/Projects/mse-repos/master-thesis/MA-Playground/samples/java/records/class/class_before.java").inputStream()
//    )
//
//    val myClass: JavaClassSource = unit.getGoverningType()
//    val anotherClass = unit.topLevelTypes[1] as JavaClassSource

//    val l: CtClass<*> =
//        Launcher.parseClass(File("/shared/home/rjenni/Projects/mse-repos/master-thesis/MA-Playground/samples/java/records/class/class_before.java").readText())


    val launcher = Launcher()

    // path can be a folder or a file
    // addInputResource can be called several times
    launcher.addInputResource("/shared/home/rjenni/Projects/mse-repos/master-thesis/MA-Playground/samples/java/records/class/class_before.java")
    launcher.environment.complianceLevel = 17

    launcher.buildModel()

    val model = launcher.model

    val test = ((model.allTypes as List<*>).first() as CtClassImpl<*>)
    val test2 = CtClassExtension(test)


    val varX = ((model.allTypes as List<*>).first() as CtClassImpl<*>).allFields[0] as CtFieldReferenceImpl
    val returnVarX =
        ((((model.allTypes as List<*>).first() as CtClassImpl<*>).allMethods.filter { !it.isShadow }[0].body.statements[0] as CtReturnImpl<*>).returnedExpression as CtFieldReadImpl).variable


    println(varX.equals(returnVarX))
}
