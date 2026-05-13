package com.geomeasure.pro.presentation.screens.map

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geomeasure.pro.presentation.theme.GeoMeasureTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mapScreenDisplaysTitle() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                MapScreen()
            }
        }
        
        composeTestRule.onNodeWithText("GeoMeasure Pro").assertExists()
    }

    @Test
    fun mapScreenHasUndoRedoButtons() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                MapScreen()
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Undo").assertExists()
        composeTestRule.onNodeWithContentDescription("Redo").assertExists()
    }

    @Test
    fun mapScreenHasFloatingActionButton() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                MapScreen()
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Start").assertExists()
    }

    @Test
    fun bottomSheetShowsMeasurementData() {
        composeTestRule.setContent {
            GeoMeasureTheme {
                MapScreen()
            }
        }
        
        composeTestRule.onNodeWithText("Measurement").assertExists()
        composeTestRule.onNodeWithText("Area").assertExists()
        composeTestRule.onNodeWithText("Perimeter").assertExists()
        composeTestRule.onNodeWithText("Vertices").assertExists()
    }
}
