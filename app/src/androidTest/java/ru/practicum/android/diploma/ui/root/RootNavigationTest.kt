package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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
import ru.practicum.android.diploma.ui.components.UiTestTags

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

        composeRule.onNodeWithTag(UiTestTags.FILTER_SCREEN).assertIsDisplayed()
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
    fun allFilterDestinationsFollowThePlannedGraph() {
        composeRule.onNodeWithContentDescription(text(R.string.search_filter_description)).performClick()
        composeRule.onNodeWithTag(UiTestTags.FILTER_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.FILTER_WORKPLACE).performClick()
        composeRule.onNodeWithTag(UiTestTags.WORKPLACE_SCREEN).assertIsDisplayed()

        composeRule.onNodeWithTag(UiTestTags.WORKPLACE_COUNTRY).performClick()
        composeRule.onNodeWithTag(UiTestTags.COUNTRY_SCREEN).assertIsDisplayed()
        pressBack()

        composeRule.onNodeWithTag(UiTestTags.WORKPLACE_REGION).performClick()
        composeRule.onNodeWithTag(UiTestTags.REGION_SCREEN).assertIsDisplayed()
        pressBack()

        pressBack()
        composeRule.onNodeWithTag(UiTestTags.FILTER_INDUSTRY).performClick()
        composeRule.onNodeWithTag(UiTestTags.INDUSTRY_SCREEN).assertIsDisplayed()

        onView(withId(R.id.bottomNavigation)).check(
            androidx.test.espresso.assertion.ViewAssertions.matches(
                withEffectiveVisibility(GONE),
            ),
        )
    }

    @Test
    fun filterActionsRemainVisibleWhenSalaryImeIsOpen() {
        composeRule.onNodeWithContentDescription(text(R.string.search_filter_description)).performClick()

        val salaryInput = composeRule.onNodeWithTag(UiTestTags.FILTER_SALARY_INPUT)
        salaryInput.performClick()
        salaryInput.performTextClearance()
        salaryInput.performTextInput("150000")

        composeRule.onNodeWithTag(UiTestTags.FILTER_APPLY).assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.FILTER_RESET).assertIsDisplayed()
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

    private fun pressBack() {
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }
}
