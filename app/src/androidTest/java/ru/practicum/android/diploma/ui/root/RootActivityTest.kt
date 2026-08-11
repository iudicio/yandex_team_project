package ru.practicum.android.diploma.ui.root

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.practicum.android.diploma.R

@RunWith(AndroidJUnit4::class)
class RootActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(RootActivity::class.java)

    @Test
    fun initialScreenShowsFigmaContent() {
        onView(withText(R.string.search_title)).check(matches(isDisplayed()))
        onView(withHint(R.string.search_hint)).check(matches(isDisplayed()))
        onView(withId(R.id.initialIllustration)).check(matches(isDisplayed()))
        onView(withId(R.id.navigationHome)).check(matches(isDisplayed()))
        onView(withId(R.id.navigationFavorites)).check(matches(isDisplayed()))
        onView(withId(R.id.navigationTeam)).check(matches(isDisplayed()))
    }

    @Test
    fun searchActionSwitchesToClearAndClearsQuery() {
        onView(withId(R.id.searchEditText)).perform(replaceText(QUERY))
        onView(withId(R.id.searchActionButton))
            .check(matches(withContentDescription(R.string.clear_search_description)))
            .perform(click())
        onView(withId(R.id.searchEditText)).check(matches(withText("")))
        onView(withId(R.id.searchActionButton))
            .check(matches(withContentDescription(R.string.search_action_description)))
    }

    private companion object {
        const val QUERY = "Android"
    }
}
