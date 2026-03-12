package com.sukes13.vendingmachine.application

@SpringBootApplication(scanBasePackages = ["be.fgov.sfpd.hedwig.no.component.scan"])
@Import(
)
class VendingMachineApplication : SpringBootServletInitializer()

fun main(args: Array<String>) {
    SpringApplication.run(HedwigApp::class.java, *args)
}