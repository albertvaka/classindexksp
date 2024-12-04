plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("com.palantir.git-version") version "3.1.0"
    id("maven-publish")
    id("java-library")
}

val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()
group = "com.albertvaka"

kotlin {
    jvmToolchain(8)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnlyApi("com.google.devtools.ksp:symbol-processing-api:2.1.0-1.0.29")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            artifactId = "classindexksp"
            pom {
                name = "ClassIndex KSP"
                description = "Index classes at compile time"
                url = "https://github.com/albertvaka/classindexksp"
                inceptionYear = "2024"
                licenses {
                    license {
                        name = "The Apache Software License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id = "albertvaka"
                        name = "Albert Vaca Cintora"
                        email = "albertvaka@gmail.com"
                    }
                }
            }
        }
    }
}
