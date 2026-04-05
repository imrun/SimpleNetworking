plugins {
    id("com.android.library") version "9.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.20" apply false
    id("org.jetbrains.dokka") version "2.2.0" apply false
}

tasks.register("publishToMavenLocal") {
    group = "publishing"
    description = "Publishes the library module to the local Maven repository."
    dependsOn(":network-sdk:publishReleasePublicationToMavenLocal")
}
