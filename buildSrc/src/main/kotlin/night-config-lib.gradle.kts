import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    `maven-publish`
    signing
	jacoco
}

repositories {
    mavenCentral()
}

// Get JUnit5 version from `libs.versions.toml`.
val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val junitVersion = versionCatalog.findVersion("junit5")
    .orElseThrow{ RuntimeException("missing version in libs.versions.toml: junit5") }
    .requiredVersion

// Use JUnit5 for all tests (that support the Gradle "JVM test suite" API, see further)
testing {
    suites {
		val test by getting(JvmTestSuite::class) {
			useJUnitJupiter(junitVersion)
		}
	}
}

// When building the JAR, also build some additional jars.
java {
    withJavadocJar()
    withSourcesJar()
}

// Add Automatic-Module-Name for JPMS support, and some other attributes for OSGI
tasks.jar {
	manifest {
		attributes["Automatic-Module-Name"] = "re.neotamia.nightconfig.${project.name}"
		attributes["Bundle-SymbolicName"] = "re.neotamia.nightconfig.${project.name}"
		attributes["Bundle-Name"] = "night-config:${project.name}"
		attributes["Bundle-Version"] = "${project.version}"
	}
}

project.afterEvaluate {
	// Enable logging for test tasks and fix mrjar tests.
	// This should be done through the "test suite" API, but it's not supported by the mrjar plugin yet.
	tasks.withType(Test::class).configureEach {
		if (this.name != "test") {
			// Since this test isn't in the JvmTestSuite, we must enable JUnit 5 "manually"
			useJUnitPlatform()
		}
		testLogging {
			events(TestLogEvent.SKIPPED, TestLogEvent.FAILED, TestLogEvent.PASSED)
		}
	}

	// Configure Jacoco with the multi-release Java versions
	tasks.withType(Test::class).filter { it.name.matches("java([0-9]+)Test".toRegex()) }.forEach {
		// it.name is a String like "java17Test"
		val prefix = it.name.removeSuffix("Test")
		val jVersion = prefix.removePrefix("java")

		val versionSpecificTestTask = it
		val versionSpecificSourceSet = project.sourceSets.getByName(prefix)
		val commonSourceSet = project.sourceSets.main.get()
		val commonTestTask = project.tasks.test.get()

		// NOTE: also run the java 11 tests, even in java 12+, because I use java 11 to write most "standard" tests
		// intended to test the common Java 8 source (because it's tedious to write java 8 in 2024).
		val java11TestTask = tasks.getByName("java11Test")

		tasks.register<JacocoReport>(prefix + "JacocoTestReport") {
			group = "Verification"
			description = "Generates coverage report for the Java ${jVersion} tests (+common, +java11)."
			dependsOn(commonTestTask, versionSpecificTestTask, java11TestTask)
			executionData(commonTestTask, versionSpecificTestTask, java11TestTask)
			//sourceSets(commonSourceSet, versionSpecificSourceSet)
			// ^^^^^^^^^ Doesn't work because jacoco doesn't understand multi-release jars.
			// Workaround: we manually add the classes without the duplicate ones (see below)

			// Fix sources

			val versionSpecificSourceDir = versionSpecificSourceSet.java.srcDirs.filter { it.name == prefix }.single() // src/main/java11, not src/java11/java
			val commonSourceDir = commonSourceSet.java.sourceDirectories.singleFile
			val versionSpecificSourceOverridesRelative = versionSpecificSourceSet.java.files.map { it.toRelativeString(versionSpecificSourceDir) }
			val fixedCommonSource = versionSpecificSourceSet.java + commonSourceSet.java.filterNot { versionSpecificSourceOverridesRelative.contains(it.toRelativeString(commonSourceDir)) }

			// Fix classes
			val versionSpecificClassDir = versionSpecificSourceSet.output.classesDirs.singleFile
			val commonClassDir = commonSourceSet.output.classesDirs.singleFile
			val versionSpecificClassOverridesRelative = versionSpecificSourceSet.output.classesDirs.asFileTree.files.map { it.toRelativeString(versionSpecificClassDir) }
			val fixedCommonClasses = files(versionSpecificSourceSet.output.classesDirs) + files(commonSourceSet.output.classesDirs.asFileTree.filterNot { versionSpecificClassOverridesRelative.contains(it.toRelativeString(commonClassDir)) })

			sourceDirectories = files(fixedCommonSource)
			classDirectories = files(fixedCommonClasses)

//			logger.warn("${sourceDirectories.files}")
//			logger.warn("${classDirectories.files}")
//			sourceDirectories = (versionSpecificSourceSet.java + commonSourceSet.java)
//			classDirectories = (versionSpecificSourceSet.output.classesDirs + commonSourceSet.output.classesDirs)
		}
	}
	tasks.jacocoTestReport {
		dependsOn(tasks.test)
	}
}

group = "re.neotamia.night-config"
version = "3.9.7"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            // automatically set:
            // groupId = project.group, artifactId = project.name, version = project.version
            pom {
                name = "NightConfig ${project.name}"
                description = "A multi-format configuration library, ${project.name} module."
                url = "https://github.com/NeoTamia/night-config"

                licenses {
                    license {
                        name = "GNU Lesser General Public License v3.0"
                        url = "https://www.gnu.org/licenses/lgpl-3.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "TheElectronWill"
                        url = "https://github.com/TheElectronWill"
                    }
                    developer {
                        id = "NeoTamia"
                        url = "https://github.com/NeoTamia"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/NeoTamia/night-config.git"
                    developerConnection = "scm:git:ssh://git@github.com:NeoTamia/night-config.git"
                    url = "https://github.com/NeoTamia/night-config"
                }
            }
            from(components["java"])
        }
    }
    repositories {
        maven {
			name = "neotamiaSnapshots"
            url = uri("https://repo.neotamia.re/snapshots")
            credentials(PasswordCredentials::class) {
                username = property("${name}Username") as String
                password = property("${name}Password") as String
            }
        }
    }
}


