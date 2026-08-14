package ru.practicum.android.diploma.ui.root

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.filter.FILTER_SETTINGS_PREFERENCES_NAME
import ru.practicum.android.diploma.ui.components.UiTestTags

@RunWith(AndroidJUnit4::class)
class RootActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<RootActivity>()

    @Before
    fun clearFilterSettings() {
        val cleared = composeRule.activity
            .getSharedPreferences(FILTER_SETTINGS_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        check(cleared)
    }

    @Test
    fun initialScreenShowsFigmaContent() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.search_title),
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.SEARCH_INPUT).assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.SEARCH_INITIAL_ILLUSTRATION).assertIsDisplayed()
        onView(withId(R.id.navigationHome)).check(matches(isDisplayed()))
        onView(withId(R.id.navigationFavorites)).check(matches(isDisplayed()))
        onView(withId(R.id.navigationTeam)).check(matches(isDisplayed()))
    }

    @Test
    fun searchActionSwitchesToClearAndClearsQuery() {
        composeRule.onNodeWithTag(UiTestTags.SEARCH_INPUT).performTextInput(QUERY)
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.clear_search_description),
        ).performClick()
        composeRule.onNodeWithTag(UiTestTags.SEARCH_INPUT).assert(
            SemanticsMatcher("search input is empty") { node ->
                node.config[SemanticsProperties.EditableText].text.isEmpty()
            },
        )
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.search_action_description),
        ).assertIsDisplayed()
    }

    @Test
    fun bottomNavigationSwitchesBetweenAllRootScreens() {
        onView(withId(R.id.navigationFavorites)).perform(click())
        composeRule.onNodeWithTag(UiTestTags.FAVORITES_SCREEN).assertIsDisplayed()

        onView(withId(R.id.navigationTeam)).perform(click())
        composeRule.onNodeWithTag(UiTestTags.TEAM_SCREEN).assertIsDisplayed()

        onView(withId(R.id.navigationHome)).perform(click())
        composeRule.onNodeWithTag(UiTestTags.SEARCH_SCREEN).assertIsDisplayed()
    }

    @Test
    fun selectedDestinationSurvivesActivityRecreation() {
        onView(withId(R.id.navigationFavorites)).perform(click())
        composeRule.onNodeWithTag(UiTestTags.FAVORITES_SCREEN).assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithTag(UiTestTags.FAVORITES_SCREEN).assertIsDisplayed()
        onView(withId(R.id.navigationFavorites)).check(matches(isSelected()))
    }

    @Test
    fun systemBackReturnsFromFavoritesToHome() {
        onView(withId(R.id.navigationFavorites)).perform(click())
        composeRule.onNodeWithTag(UiTestTags.FAVORITES_SCREEN).assertIsDisplayed()

        pressBack()

        composeRule.onNodeWithTag(UiTestTags.SEARCH_SCREEN).assertIsDisplayed()
        onView(withId(R.id.navigationHome)).check(matches(isSelected()))
    }

    @Test
    fun searchQuerySurvivesRootTabSwitch() {
        composeRule.onNodeWithTag(UiTestTags.SEARCH_INPUT).performTextInput(QUERY)
        closeSoftKeyboard()

        onView(withId(R.id.navigationFavorites)).perform(click())
        onView(withId(R.id.navigationHome)).perform(click())

        composeRule.onNodeWithTag(UiTestTags.SEARCH_INPUT).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.EditableText,
                AnnotatedString(QUERY),
            ),
        )
    }

    @Test
    fun filterScreenHidesBottomNavigationAndHeaderBackReturnsToSearch() {
        openFilterScreen()

        composeRule.onNodeWithTag(UiTestTags.FILTER_SCREEN).assertIsDisplayed()
        onView(withId(R.id.bottomNavigation)).check(matches(not(isDisplayed())))

        composeRule.onNodeWithTag(UiTestTags.FILTER_BACK).performClick()

        composeRule.onNodeWithTag(UiTestTags.SEARCH_SCREEN).assertIsDisplayed()
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
    }

    @Test
    fun salaryKeepsDigitsAndClearHidesFilterActions() {
        openFilterScreen()

        composeRule.onNodeWithTag(UiTestTags.FILTER_SALARY).performTextInput(SALARY_WITH_NON_DIGITS)

        composeRule.onNodeWithTag(UiTestTags.FILTER_SALARY).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.EditableText,
                AnnotatedString(FILTERED_SALARY),
            ),
        )
        composeRule.onNodeWithTag(UiTestTags.FILTER_RESET).assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.FILTER_APPLY).assertIsDisplayed()

        composeRule.onNodeWithTag(UiTestTags.FILTER_SALARY_CLEAR).performClick()

        composeRule.onNodeWithTag(UiTestTags.FILTER_SALARY).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.EditableText,
                AnnotatedString(""),
            ),
        )
        composeRule.onNodeWithTag(UiTestTags.FILTER_RESET).assertDoesNotExist()
        composeRule.onNodeWithTag(UiTestTags.FILTER_APPLY).assertDoesNotExist()
    }

    @Test
    fun onlyWithSalaryAndResetUpdateCheckboxAndFilterActions() {
        openFilterScreen()

        composeRule.onNodeWithTag(UiTestTags.FILTER_ONLY_WITH_SALARY).assertIsOff()
        composeRule.onNodeWithTag(UiTestTags.FILTER_ONLY_WITH_SALARY).performClick()

        composeRule.onNodeWithTag(UiTestTags.FILTER_ONLY_WITH_SALARY).assertIsOn()
        composeRule.onNodeWithTag(UiTestTags.FILTER_RESET).assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTags.FILTER_APPLY).assertIsDisplayed()

        composeRule.onNodeWithTag(UiTestTags.FILTER_RESET).performClick()

        composeRule.onNodeWithTag(UiTestTags.FILTER_ONLY_WITH_SALARY).assertIsOff()
        composeRule.onNodeWithTag(UiTestTags.FILTER_RESET).assertDoesNotExist()
        composeRule.onNodeWithTag(UiTestTags.FILTER_APPLY).assertDoesNotExist()
    }

    @Test
    fun countryAndRegionScreensOpenInsideSingleActivity() {
        openFilterScreen()

        composeRule.onNodeWithTag(UiTestTags.FILTER_WORKPLACE).performClick()
        composeRule.onNodeWithTag(UiTestTags.WORKPLACE_SCREEN).assertIsDisplayed()
        onView(withId(R.id.bottomNavigation)).check(matches(not(isDisplayed())))

        composeRule.onNodeWithTag(UiTestTags.WORKPLACE_COUNTRY).performClick()
        composeRule.onNodeWithTag(UiTestTags.COUNTRY_SCREEN).assertIsDisplayed()
        pressBack()

        composeRule.onNodeWithTag(UiTestTags.WORKPLACE_REGION).performClick()
        composeRule.onNodeWithTag(UiTestTags.REGION_SCREEN).assertIsDisplayed()
        onView(withId(R.id.bottomNavigation)).check(matches(not(isDisplayed())))
    }

    @Test
    fun applyPersistsFilterAndReturnsToSearch() {
        openFilterScreen()
        composeRule.onNodeWithTag(UiTestTags.FILTER_ONLY_WITH_SALARY).performClick()

        composeRule.onNodeWithTag(UiTestTags.FILTER_APPLY).performClick()

        composeRule.onNodeWithTag(UiTestTags.SEARCH_SCREEN).assertIsDisplayed()
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
        openFilterScreen()
        composeRule.onNodeWithTag(UiTestTags.FILTER_ONLY_WITH_SALARY).assertIsOn()
    }

    private fun openFilterScreen() {
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.filter_description),
        ).performClick()
        composeRule.onNodeWithTag(UiTestTags.FILTER_SCREEN).assertIsDisplayed()
    }

    private companion object {
        const val QUERY = "Android"
        const val SALARY_WITH_NON_DIGITS = "12a3.4"
        const val FILTERED_SALARY = "1234"
    }
}
