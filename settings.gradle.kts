pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net")
		maven("https://maven.legacyfabric.net/")
		maven("https://maven.kikugie.dev/snapshots")
		maven("https://maven.kikugie.dev/releases")
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.8.3"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"
	create(rootProject) {
		fun mc(mcVersion: String, loaders: Iterable<String>) {
			for (loader in loaders) {
                version("$mcVersion-$loader", mcVersion)
			}
		}

		mc("1.8.9", listOf("fabric"))

		vcsVersion = "1.8.9-fabric"
	}
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}

rootProject.name = "Legacy Stonecutter Template"
