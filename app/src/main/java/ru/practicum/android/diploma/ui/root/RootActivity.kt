package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showBottomNavigation = destination.hierarchy.any { navDestination ->
                navDestination.id in TOP_LEVEL_DESTINATIONS
            }
            binding.bottomNavigation.isVisible = showBottomNavigation
            binding.bottomNavigationDivider.isVisible = showBottomNavigation
        }
    }

    private companion object {
        val TOP_LEVEL_DESTINATIONS: Set<Int> = setOf(
            R.id.navigationHome,
            R.id.navigationFavorites,
            R.id.navigationTeam,
        )
    }
}
