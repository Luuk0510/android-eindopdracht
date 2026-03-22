package com.luuk.showtracker.ui.navigation

// Dit object bevat alle routes. Als we later een route willen veranderen, 
// hoeven we dat maar op één plek te doen.
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Saved : Screen("saved")
    object Details : Screen("details/{title}/{overview}/{poster}") {
        // Functie om de route te maken met data erin
        fun createRoute(title: String, overview: String, poster: String): String {
            return "details/$title/$overview/$poster"
        }
    }
}