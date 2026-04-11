package com.luuk.showtracker.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTest {

    @Test
    fun detailsCreateRoute_addsItemIdToRoute() {
        val route = Screen.Details.createRoute(123)

        assertEquals("details/123", route)
    }

    @Test
    fun detailsRoute_hasIdArgumentPlaceholder() {
        assertEquals("details/{id}", Screen.Details.route)
    }

    @Test
    fun topLevelRoutes_haveExpectedValues() {
        assertEquals("home", Screen.Home.route)
        assertEquals("saved", Screen.Saved.route)
    }
}
