plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.8.9-fabric" /* [SC] DO NOT EDIT */

stonecutter {
    parameters {
        val loader = node.project.property("loader.platform")
        constants["fabric"] = loader == "fabric"
    }
}