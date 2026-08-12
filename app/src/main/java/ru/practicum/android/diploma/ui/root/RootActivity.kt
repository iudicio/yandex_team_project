package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ActivityRootBinding
import ru.practicum.android.diploma.ui.favorites.FavoritesFragment
import ru.practicum.android.diploma.ui.search.SearchFragment

class RootActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentContainer, SearchFragment())
                .commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigationHome -> true
                R.id.navigationFavorites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.contentContainer, FavoritesFragment())
                        .commit()
                    true
                }
                R.id.navigationTeam,
                -> {
                    Toast.makeText(
                        this,
                        getString(R.string.section_is_in_development, item.title),
                        Toast.LENGTH_SHORT,
                    ).show()
                    false
                }

                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.navigationHome
    }
}
