package com.geomeasure.pro.presentation.screens.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geomeasure.pro.presentation.theme.GeoMeasureTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreenDisplaysTitle() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                SettingsScreen()
            }
        }
        
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun settingsScreenShowsAllGroups() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                SettingsScreen()
            }
        }
        
        composeTestRule.onNodeWithText("Units").assertExists()
        composeTestRule.onNodeWithText("GPS").assertExists()
        composeTestRule.onNodeWithText("Map").assertExists()
        composeTestRule.onNodeWithText("Appearance").assertExists()
        composeTestRule.onNodeWithText("Data & Privacy").assertExists()
    }

    @Test
    fun settingsHasDarkModeSwitch() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                SettingsScreen()
            }
        }
        
        composeTestRule.onNodeWithText("Dark Mode").assertExists()
    }

    @Test
    fun settingsHasBackupButton() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                SettingsScreen()
            }
        }
        
        composeTestRule.onNodeWithText("Export Encrypted Backup").assertExists()
        composeTestRule.onNodeWithText("Restore from Backup").assertExists()
    }

    @Test
    fun settingsHasDeleteAllButton() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                SettingsScreen()
            }
        }
        
        composeTestRule.onNodeWithText("Delete All Data").assertExists()
    }
}
