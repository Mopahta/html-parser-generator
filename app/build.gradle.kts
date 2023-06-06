plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("com.j2html:j2html:1.6.0")
}

application {
    // Define the main class for the application.
    mainClass.set("betbonanza.app.App")
}
