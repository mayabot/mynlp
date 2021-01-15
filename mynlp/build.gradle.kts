plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDirs(this.kotlin.srcDirs.filter { it.name == "java" })
        }
    }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib")

    compileOnly("org.apache.lucene:lucene-core:5.0.0")

    //    // logs
    compileOnly(group = "org.slf4j", name = "slf4j-api", version = "1.7.21")
    compileOnly(group = "commons-logging", name = "commons-logging", version = "1.2")
    compileOnly(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.6.2")
    compileOnly(group = "log4j", name = "log4j", version = "1.2.17")

    compileOnly("net.openhft:zero-allocation-hashing:0.12")
    compileOnly(group = "org.fusesource.jansi", name = "jansi", version = "1.16")

    testImplementation("junit:junit:4.12")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("com.mayabot.mynlp.resource:mynlp-resource-cws:1.0.0")
    testImplementation("org.apache.lucene:lucene-core:5.0.0")

    testImplementation("net.openhft:zero-allocation-hashing:0.12")

    // 核心词典
    testImplementation("com.mayabot.mynlp.resource:mynlp-resource-coredict:1.0.0")
    // 词性标注
    testImplementation("com.mayabot.mynlp.resource:mynlp-resource-pos:1.0.0")
    // 命名实体
    testImplementation("com.mayabot.mynlp.resource:mynlp-resource-ner:1.0.0")
    // pinyin
    testImplementation("com.mayabot.mynlp.resource:mynlp-resource-pinyin:1.1.0")
    // 繁简体转换
    testImplementation("com.mayabot.mynlp.resource:mynlp-resource-transform:1.0.0")
    // 感知机分词模型
    testImplementation("com.mayabot.mynlp.resource:mynlp-resource-cws:1.0.0")
    // 自定义扩展词库
    testImplementation("com.mayabot.mynlp.resource:mynlp-resource-custom:1.0.0")
}


tasks {
    test {
        // 默认单个Test的内存是500M左右
        maxHeapSize = "1G"
    }
    jar {
        manifest {
            attributes["Main-Class"] = "com.mayabot.nlp.cli.MynlpCliKt"
        }
    }
    shadowJar {
        archiveClassifier.set("bin")
    }
}