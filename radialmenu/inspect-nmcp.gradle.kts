nmcp {
    publishAllPublications {
        username.set((project.findProperty("ossrhUsername") as? String) ?: "")
        password.set((project.findProperty("ossrhPassword") as? String) ?: "")
        publicationType.set("USER_MANAGED")
    }
}