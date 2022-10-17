import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

        plugins {
            kotlin("jvm") version "1.7.20"

            application
            id("com.github.johnrengelman.shadow") version "7.1.2"
        }

group = "io.chast.refactor.java.refactorings.class_to_record"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":base"))
    implementation("org.jboss.forge.roaster:roaster-jdt:2.26.0.Final")

    implementation("fr.inria.gforge.spoon:spoon-core:10.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("MainKt")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}

tasks.shadowJar {
    minimize()
}
