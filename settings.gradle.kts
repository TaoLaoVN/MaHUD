pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CPUMonitorPro"

// Application
include(":app")

// Domain & Data
include(":domain")
include(":data")

// Core modules
include(":core:common")
include(":core:designsystem")
include(":core:ui")
include(":core:database")
include(":core:datastore")
include(":core:monitoring")
include(":core:charts")
include(":core:network")
include(":core:logging")
include(":core:analytics")
include(":core:testing")

// Feature modules
include(":feature-dashboard")
include(":feature-cpu")
include(":feature-memory")
include(":feature-battery")
include(":feature-thermal")
include(":feature-storage")
include(":feature-process")
include(":feature-deviceinfo")
include(":feature-benchmark")
include(":feature-history")
include(":feature-export")
include(":feature-overlay")
include(":feature-alerts")
include(":feature-analytics")
include(":feature-settings")

// Service modules
include(":service-monitoring")
include(":service-overlay")
