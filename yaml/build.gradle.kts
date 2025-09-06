plugins {
    id("night-config-lib")
	id("me.champeau.mrjar") version "0.1.1"
}

multiRelease {
    targetVersions(21)
}

dependencies {
	api(project(":core"))
	implementation(libs.snakeYamlEngine)

	testImplementation(project(":test-shared"))
}
