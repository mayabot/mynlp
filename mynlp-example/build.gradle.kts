description = "Example"

project.afterEvaluate {
    project.tasks.withType<AbstractPublishToMaven>{
        enabled = false
    }
}

dependencies {

    implementation(project(":mynlp-all"))

    implementation( "com.mayabot.mynlp.resource:mynlp-resource-cws:1.0.0")
    implementation( "com.mayabot.mynlp.resource:mynlp-resource-custom:1.0.0")
    implementation( "org.fusesource.jansi:jansi:1.16")
    implementation( "ch.qos.logback:logback-classic:1.2.3")


}