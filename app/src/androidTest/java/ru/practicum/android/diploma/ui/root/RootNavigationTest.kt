package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.fragment.NavHostFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.ui.common.VACANCY_ID_ARGUMENT

@RunWith(AndroidJUnit4::class)
class RootNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<RootActivity>()

    @Test
    fun bottomTabsSwitchTopLevelScreens() {
        composeRule.onNodeWithText(text(R.string.search_title)).assertIsDisplayed()

        onView(withId(R.id.navigationFavorites)).perform(click())
        composeRule.onNodeWithText(text(R.string.favorites_title)).assertIsDisplayed()

        onView(withId(R.id.navigationTeam)).perform(click())
        composeRule.onNodeWithText(text(R.string.team_title)).assertIsDisplayed()

        onView(withId(R.id.navigationSearch)).perform(click())
        composeRule.onNodeWithText(text(R.string.search_title)).assertIsDisplayed()
    }

    @Test
    fun nestedDestinationHidesBottomNavigationAndBackRestoresIt() {
        composeRule.onNodeWithContentDescription(text(R.string.search_filter_description)).performClick()

        composeRule.onNodeWithText(text(R.string.screen_filter)).assertIsDisplayed()
        onView(withId(R.id.bottomNavigation)).check(
            androidx.test.espresso.assertion.ViewAssertions.matches(
                withEffectiveVisibility(GONE),
            ),
        )

        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithText(text(R.string.search_title)).assertIsDisplayed()
        onView(withId(R.id.bottomNavigation)).check(
            androidx.test.espresso.assertion.ViewAssertions.matches(isDisplayed()),
        )
    }

    @Test
    fun detailsDestinationReceivesVacancyId() {
        composeRule.activityRule.scenario.onActivity { activity ->
            val navHost = activity.supportFragmentManager
                .findFragmentById(R.id.navHostFragment) as NavHostFragment
            navHost.navController.navigate(
                R.id.detailsFragment,
                Bundle().apply {
                    putString(VACANCY_ID_ARGUMENT, "test-vacancy")
                },
            )
        }

        composeRule.onNodeWithText(text(R.string.details_toolbar_title)).assertIsDisplayed()
        onView(withId(R.id.bottomNavigation)).check(
            androidx.test.espresso.assertion.ViewAssertions.matches(
                withEffectiveVisibility(GONE),
            ),
        )
    }

    @Test
    fun allNestedPlaceholdersFollowThePlannedGraph() {
        composeRule.onNodeWithContentDescription(text(R.string.search_filter_description)).performClick()
        composeRule.onNodeWithText(text(R.string.action_open_workplace)).performClick()

        composeRule.onNodeWithText(text(R.string.action_open_country)).performClick()
        composeRule.onNodeWithText(text(R.string.screen_country)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_back)).performClick()

        composeRule.onNodeWithText(text(R.string.action_open_region)).performClick()
        composeRule.onNodeWithText(text(R.string.screen_region)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_back)).performClick()

        composeRule.onNodeWithText(text(R.string.action_back)).performClick()
        composeRule.onNodeWithText(text(R.string.action_open_industry)).performClick()
        composeRule.onNodeWithText(text(R.string.screen_industry)).assertIsDisplayed()

        onView(withId(R.id.bottomNavigation)).check(
            androidx.test.espresso.assertion.ViewAssertions.matches(
                withEffectiveVisibility(GONE),
            ),
        )
    }

    @Test
    fun selectedTabSurvivesActivityRecreation() {
        onView(withId(R.id.navigationFavorites)).perform(click())
        composeRule.onNodeWithText(text(R.string.favorites_title)).assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText(text(R.string.favorites_title)).assertIsDisplayed()
        onView(withId(R.id.bottomNavigation)).check(
            androidx.test.espresso.assertion.ViewAssertions.matches(isDisplayed()),
        )
        onView(withId(R.id.navigationFavorites)).check(
            androidx.test.espresso.assertion.ViewAssertions.matches(isSelected()),
        )
    }

    private fun text(resourceId: Int, vararg formatArgs: Any): String =
        composeRule.activity.getString(resourceId, *formatArgs)
}
