plugins {
    java
    `maven-publish`
}

group = "me.notom3ga"
version = "1.0"

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(11)
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components.getByName("java"))
        }
    }

    repositories.maven {
        name = "notOM3GARepo"
        url = uri("https://notom3ga.me/repo/snapshots/")
        credentials(PasswordCredentials::class)
    }
}
