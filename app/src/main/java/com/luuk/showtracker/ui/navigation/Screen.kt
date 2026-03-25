package com.luuk.showtracker.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Saved : Screen("saved")
    object Details : Screen("details/{id}/{title}/{overview}/{poster}/{genres}/{releaseDate}") {
        fun createRoute(
            id: Int,
            title: String,
            overview: String,
            poster: String,
            genres: String,
            releaseDate: String
        ): String {
            return "details/$id/$title/$overview/$poster/$genres/$releaseDate"
        }
    }
}
