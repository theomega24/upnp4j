import java.util.Locale

plugins {
    java
    `maven-publish`
    signing
}

group = "dev.omega24"
version = "1.0"

tasks.withType<JavaCompile>().configureEach {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(11)
}

tasks.withType<Javadoc> {
    options.encoding = Charsets.UTF_8.name()
}
val javadocJar by tasks.creating(Jar::class) {
    dependsOn.add(tasks.javadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name.toLowerCase(Locale.ENGLISH)
            from(components["java"])
            artifact(javadocJar)
            artifact(sourcesJar)

            pom {
                name.set("upnp4j")
                description.set("A simple UPnP library for Java")
                url.set("https://github.com/theomega24/upnp4j")

                licenses {
                    license {
                        name.set("LGPL 3.0")
                        url.set("https://opensource.org/licenses/LGPL-3.0")
                    }
                }

                developers {
                    developer {
                        id.set("omega24")
                        name.set("Ben Kerllenevich")
                        email.set("ben@omega24.dev")
                    }
                }

                scm {
                    url.set("https://github.com/theomega24/upnp4j")
                    connection.set("scm:https://github.com/theomega24/upnp4j.git")
                    developerConnection.set("scm:git://github.com/theomega24/upnp4j.git")
                }
            }
        }
    }

    repositories.maven {
        name = "ossrh"
        url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        credentials(PasswordCredentials::class)
    }
}

signing {
    sign(publishing.publications)
}
