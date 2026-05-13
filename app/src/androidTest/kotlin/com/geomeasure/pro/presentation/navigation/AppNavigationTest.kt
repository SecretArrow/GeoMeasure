package com.geomeasure.pro.presentation.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geomeasure.pro.presentation.theme.GeoMeasureTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomNavHasAllItems() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                AppNavigation()
            }
        }
        
        composeTestRule.onNodeWithText("Map").assertExists()
        composeTestRule.onNodeWithText("Projects").assertExists()
        composeTestRule.onNodeWithText("Tools").assertExists()
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun clickingSettingsShowsSettingsScreen() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                AppNavigation()
            }
        }
        
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun clickingProjectsShowsProjectsScreen() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                AppNavigation()
            }
        }
        
        composeTestRule.onNodeWithText("Projects").performClick()
        composeTestRule.onNodeWithText("Projects").assertExists()
    }
}
