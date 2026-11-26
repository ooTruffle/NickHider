val modName = project.property("mod_name") as String
val modId = project.property("mod_id") as String
val modVersion = project.property("mod_version") as String
val modDescription = project.property("mod_description") as String
val modArchivesName = project.property("mod_archives_name") as String
val baseGroup = project.property("base_group") as String

val javaVersion = project.property("java_version") as String
val minecraftVersion = project.property("minecraft_version") as String
val fabricLoaderVersion = project.property("fabric_loader_version") as String
val oslVersion = project.property("osl_version") as String

val oneconfigVersion = project.property("oneconfig_version") as String

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
    id("ploceus") version "1.17-SNAPSHOT"
}

ploceus {
    setIntermediaryGeneration(2)
}

group = baseGroup
version = "$modVersion+$minecraftVersion"

base {
    archivesName.set("$modArchivesName-${minecraftVersion}-ornithe")
}

repositories {
    mavenCentral()
    google()

    // OneConfig's Ornithe artifact pulls in Compose Multiplatform/androidx and lenis from here.
    maven("https://maven.cloverclient.com/releases")
    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.polyfrost.org/snapshots")
    maven("https://maven.fabricmc.net/")
}

loom {
    runConfigs.remove(runConfigs["server"])
    runConfigs.all {
        runDirectory = rootProject.file("run")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // MCP names
    mappings(ploceus.mcpMappings("stable", minecraftVersion, "22"))
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    ploceus.dependOsl(oslVersion)

    modImplementation("org.polyfrost.oneconfig:$minecraftVersion-ornithe:$oneconfigVersion")
    for (module in arrayOf("commands", "config", "config-impl", "events", "internal", "ui", "utils", "hud")) {
        implementation("org.polyfrost.oneconfig:$module:$oneconfigVersion")
    }
}

tasks.processResources {
    val props = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to modVersion,
        "mod_description" to modDescription,
        "base_group" to baseGroup,
        "java_version" to javaVersion,
        "minecraft_version" to minecraftVersion,
        "fabric_loader_version" to fabricLoaderVersion,
        "osl_version" to oslVersion,
        "oneconfig_version" to oneconfigVersion
    )

    inputs.properties(props)

    filesMatching(listOf("fabric.mod.json", "mixins.$modId.json", "mixins.$modId.argentum.json")) {
        expand(props)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaVersion.toInt()
    options.encoding = "UTF-8"
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks.jar {
    inputs.property("archivesName", base.archivesName)
}
