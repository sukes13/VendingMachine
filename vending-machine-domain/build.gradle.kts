project(":vending-machine-domain") {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.set(listOf("-Xjsr305=strict"))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        "implementation"(platform("org.springframework.boot:spring-boot-dependencies:3.3.5"))

        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
        "testImplementation"("org.assertj:assertj-core")
        "testImplementation"("org.awaitility:awaitility")
    }
}