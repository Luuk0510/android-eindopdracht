package com.luuk.showtracker.ui.navigation

// Dit object bevat alle routes. Als we later een route willen veranderen, 
// hoeven we dat maar op één plek te doen.
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Saved : Screen("saved")
    object Details : Screen("details/{id}/{title}/{overview}/{poster}/{genres}") {
        fun createRoute(id: Int, title: String, overview: String, poster: String, genres: String): String {
            return "details/$id/$title/$overview/$poster/$genres"
        }
    }
}
