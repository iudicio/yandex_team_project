package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.practicum.android.diploma.R

class RootActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val bottomNavigationDivider = findViewById<View>(R.id.bottomNavigationDivider)

        bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevelDestination = destination.hierarchy.any { navDestination ->
                navDestination.id in TOP_LEVEL_DESTINATIONS
            }
            bottomNavigation.isVisible = isTopLevelDestination
            bottomNavigationDivider.isVisible = isTopLevelDestination
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }

    private companion object {
        val TOP_LEVEL_DESTINATIONS = setOf(
            R.id.navigationSearch,
            R.id.navigationFavorites,
            R.id.navigationTeam,
        )
    }
}
