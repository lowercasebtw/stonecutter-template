@file:OptIn(StonecutterExperimentalAPI::class)

import dev.kikugie.stonecutter.StonecutterExperimentalAPI

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.loom.remap)
    alias(libs.plugins.legacy.looming)
    id("maven-publish")
}

class ModData {
    val id = property("mod.id") as String
    val name = property("mod.name") as String
    val version = property("mod.version") as String
    val group = property("mod.group") as String
    val description = property("mod.description") as String
    val discord = property("mod.discord") as String
    val minecraftVersion = property("mod.minecraft_version") as String
    val minecraftVersionRange = property("mod.minecraft_version_range") as String
}

class Dependencies {
    val fabricLoaderVersion = property("deps.fabric_loader_version") as String
    val devAuthVersion = property("deps.devauth_version") as String
    val mappingsVersion = property("deps.mappings_version") as String
}

val mod = ModData()
val deps = Dependencies()

class LoaderData {
    val name = property("loader.platform") as String?
    val isFabric = "fabric".equals(name, ignoreCase = true)
}

val loader = LoaderData()

val versionString = "${mod.version}-${mod.minecraftVersion}_${loader.name}"
group = mod.group
base {
    archivesName.set("${mod.id}-${versionString}")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") // DevAuth
    maven("https://maven.terraformersmc.com/") // Mod Menu
}

dependencies {
    minecraft("com.mojang:minecraft:${mod.minecraftVersion}")
    mappings(legacy.yarn(mod.minecraftVersion, deps.mappingsVersion))

    modRuntimeOnly("me.djtheredstoner:DevAuth-${loader.name}:${deps.devAuthVersion}")
    if (loader.isFabric) {
        modImplementation("net.fabricmc:fabric-loader:${deps.fabricLoaderVersion}")
    }
}

loom {
    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        runDir = "../../run"
    }

    runConfigs.remove(runConfigs["server"]) // Removes server run configs
    accessWidenerPath = stonecutter.process(
        rootProject.file("src/main/resources/${mod.id}.accesswidener"),
        "build/processed.accesswidener"
    )

    runs {
        afterEvaluate {
            configureEach {
                property("mixin.hotSwap", "true")
                property("mixin.debug.export", "true") // Puts mixin outputs in /run/.mixin.out
                property("devauth.enabled", "true")
                property("devauth.account", "main")
            }
        }
    }
}

java {
    val requiredJava = JavaVersion.VERSION_1_8
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
    withSourcesJar()
}

tasks {
    processResources {
        val props = buildMap {
            put("id", mod.id)
            put("name", mod.name)
            put("version", mod.version)
            put("description", mod.description)
            put("discord", mod.discord)
            put("minecraft_version_range", mod.minecraftVersionRange)
            if (loader.isFabric) {
                put("fabric_loader_version", deps.fabricLoaderVersion)
            }
        }

        props.forEach(inputs::property)
        filesMatching("**/lang/en_us.json") { // Defaults description to English translation
            expand(props)
            filteringCharset = "UTF-8"
        }

        if (loader.isFabric) {
            filesMatching("fabric.mod.json") { expand(props) }
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar, remapSourcesJar)
        into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
        dependsOn("build")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = mod.id
            group = project.group
            version = versionString
            from(components["java"])
        }
    }

    repositories {}
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(tasks.named("build"))
    }
}

fun <T> optionalProp(property: String, block: (String) -> T?): T? =
    findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)