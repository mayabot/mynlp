import org.jetbrains.kotlin.backend.common.phaser.namedUnitPhase
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20" apply false
    id("idea")
    id("eclipse")
}

description = "mynlp是mayabot开源的中文自然语言处理工具集"

val buildVersion = "3.4.0"
val snapShot = true

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {

    apply(plugin="org.jetbrains.kotlin.jvm")

    description = "Maya Nlp subproject ${project.path}"

    group = "com.mayabot.mynlp"
    version = buildVersion + if(snapShot) "-SNAPSHOT" else ""

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }


    val publishIt = this.name != "mynlp-example"

    if (publishIt) {
        apply(plugin="maven-publish")
        apply(plugin="signing")

        configure<JavaPluginExtension> {
            withJavadocJar()
            withSourcesJar()
        }

        configure<PublishingExtension>{
            publications{

                create<MavenPublication>("java") {
                    from(components["java"])
                    versionMapping {
                        usage("java-api") {
                            fromResolutionOf("runtimeClasspath")
                        }
                        usage("java-runtime") {
                            fromResolutionResult()
                        }
                    }

                    repositories {

                        if (project.hasProperty("maya_pri_user")) {
                            maven {
                                name = "MayaPrivate"
                                if (snapShot) {
                                    setUrl(project.property("maya_pri_snapshot") as String)
                                } else {
                                    setUrl(project.property("maya_pri_release") as String)
                                }
                                credentials {
                                    username = project.property("maya_pri_user") as String
                                    password = project.property("maya_pri_pass") as String
                                }
                            }
                        }

                        if (!snapShot && project.hasProperty("oss_user")) {
                            maven {
                                name = "OssPublic"
                                if (snapShot) {
                                    setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
                                } else {
                                    setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                                }

                                credentials {
                                    username = project.property("oss_user") as String
                                    password = project.property("oss_password") as String
                                }
                            }
                        }

//                        if (!snapShot && project.hasProperty("github_token")) {
//                            maven {
//                                name 'GithubPublic'
//                                url = "https://maven.pkg.github.com/mayabot/mynlp"
//
//                                credentials {
//                                    username "jimichan"
//                                    password "${github_token}"
//                                }
//                            }
//                        }
                    }

                    pom {
                        name.set("mynlp")
                        description.set(project.description)
                        url.set("https://github.com/mayabot/mynlp")
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("jimichan")
                                name.set("Jimi chan")
                                email.set("jimichan@gmail.com")
                            }
                        }
                        scm {
                            connection.set("scm:git:git@github.com:mayabot/mynlp.git")
                            developerConnection.set("scm:git:git@github.com:mayabot/mynlp.git")
                            url.set("git@github.com:mayabot/mynlp.git")
                        }
                    }
                }
            }
        }

        if(project.hasProperty("signing.keyId")){
            configure<SigningExtension>{
                sign(the<PublishingExtension>().publications["java"])
            }
        }

        if (JavaVersion.current().isJava8Compatible) {
            tasks.withType<Javadoc>{
                //options.addStringOption('Xdoclint:none', '-quiet')
                options{
                    (this as CoreJavadocOptions).addStringOption("Xdoclint:none","-quiet")
                    encoding = "UTF-8"
                    quiet()
                    charset("UTF-8")
                }


            }
            tasks.withType<JavaCompile>{
                options.encoding = "UTF-8"
                options.compilerArgs = options.compilerArgs + listOf("-Xdoclint:none", "-Xlint:none", "-nowarn")
            }
        }
    }
}



