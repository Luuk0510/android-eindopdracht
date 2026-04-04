package com.luuk.showtracker.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Saved : Screen("saved")
    object Details : Screen("details/{id}") {
        fun createRoute(id: Int): String {
            return "details/$id"
        }
    }
}
