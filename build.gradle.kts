import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target

plugins {
    id("java")
    id("org.liquibase.gradle") version "2.2.0"
    id("nu.studer.jooq") version "9.0"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "anton.asmirko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("org.springframework:spring-context:6.2.12")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
    implementation("org.jooq:jooq:3.19.9")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jooqGenerator("org.jooq:jooq-meta:3.19.9")
    jooqGenerator("org.jooq:jooq-meta-extensions-liquibase:3.19.9") // optional
    jooqGenerator("org.jooq:jooq-codegen:3.19.9")
    jooqGenerator("org.xerial:sqlite-jdbc:3.50.3.0")

    liquibaseRuntime("org.liquibase:liquibase-core:4.28.0")
    liquibaseRuntime("info.picocli:picocli:4.7.5")
    liquibaseRuntime("org.xerial:sqlite-jdbc:3.50.3.0")
}

spotless {
    java {
        target("**/*.java")
        targetExclude("build/generated/**/*", "src/generated/**/*")
        googleJavaFormat("1.17.0")
        importOrder()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.check {
    dependsOn("spotlessCheck")
}

liquibase {
    activities.register("main") {
        this.arguments = mapOf(
            "changeLogFile" to "src/main/resources/db/db.changelog-master.xml",
            "url" to "jdbc:sqlite:${project.findProperty("db.path")}",
            "driver" to "org.sqlite.JDBC"
        )
    }
    runList = "main"
}

jooq {
    version.set("3.19.9")

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                jdbc = Jdbc().apply {
                    driver = "org.sqlite.JDBC"
                    url = "jdbc:sqlite:${project.findProperty("db.path")}"
                }
                generator = Generator().apply {
                    name = "org.jooq.codegen.JavaGenerator"
                    database = Database().apply {
                        name = "org.jooq.meta.sqlite.SQLiteDatabase"
                        inputSchema = "" // SQLite has no schemas
                    }
                    target = Target().apply {
                        packageName = "com.example.jooq.generated"
                        directory = "src/generated/java"
                    }
                }
            }
        }
    }
}

tasks.named("generateJooq") {
    dependsOn("update")
}

sourceSets {
    named("main") {
        java.srcDir("src/generated/java")
    }
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("shortlinks")
    archiveVersion.set("")
    archiveClassifier.set("")
    from(sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    dependsOn(configurations.runtimeClasspath)

    from({
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    })

    manifest {
        attributes["Main-Class"] = "anton.asmirko.Main"
    }
}

tasks.test {
    useJUnitPlatform()
}
