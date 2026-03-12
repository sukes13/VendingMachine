plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "com.sukes13.vendingmachine"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

//subprojects {
//    apply(plugin = "org.jetbrains.kotlin.jvm")
//
//    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
//        compilerOptions {
//            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
//            freeCompilerArgs.set(listOf("-Xjsr305=strict"))
//        }
//    }
//
//    tasks.withType<Test> {
//        useJUnitPlatform()
//    }
//}
