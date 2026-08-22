package ru.practicum.android.diploma.data.filter

import android.content.SharedPreferences

internal class InMemorySharedPreferences(
    initialValues: Map<String, Any?> = emptyMap(),
) : SharedPreferences {
    private val values = initialValues.toMutableMap()
    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    val snapshot: Map<String, Any?>
        get() = values.toMap()

    override fun getAll(): MutableMap<String, *> = values.toMutableMap()

    override fun getString(key: String, defValue: String?): String? = values[key] as? String ?: defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? =
        (values[key] as? Set<String>)?.toMutableSet() ?: defValues

    override fun getInt(key: String, defValue: Int): Int = values[key] as? Int ?: defValue

    override fun getLong(key: String, defValue: Long): Long = values[key] as? Long ?: defValue

    override fun getFloat(key: String, defValue: Float): Float = values[key] as? Float ?: defValue

    override fun getBoolean(key: String, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue

    override fun contains(key: String): Boolean = key in values

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        listeners += listener
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        listeners -= listener
    }

    private inner class Editor : SharedPreferences.Editor {
        private val updates = mutableMapOf<String, Any?>()
        private val removals = mutableSetOf<String>()
        private var clearRequested = false

        override fun putString(key: String, value: String?): SharedPreferences.Editor = update(key, value)

        override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor =
            update(key, values?.toSet())

        override fun putInt(key: String, value: Int): SharedPreferences.Editor = update(key, value)

        override fun putLong(key: String, value: Long): SharedPreferences.Editor = update(key, value)

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = update(key, value)

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = update(key, value)

        override fun remove(key: String): SharedPreferences.Editor = also {
            updates -= key
            removals += key
        }

        override fun clear(): SharedPreferences.Editor = also {
            clearRequested = true
            updates.clear()
            removals.clear()
        }

        override fun commit(): Boolean {
            applyChanges()
            return true
        }

        override fun apply() = applyChanges()

        private fun update(key: String, value: Any?): SharedPreferences.Editor = also {
            removals -= key
            updates[key] = value
        }

        private fun applyChanges() {
            val changedKeys = mutableSetOf<String>()
            clearValues(changedKeys)
            removeValues(changedKeys)
            updateValues(changedKeys)
            notifyListeners(changedKeys)
        }

        private fun clearValues(changedKeys: MutableSet<String>) {
            if (clearRequested) {
                changedKeys += values.keys
                values.clear()
            }
        }

        private fun removeValues(changedKeys: MutableSet<String>) {
            removals.forEach { key ->
                if (values.remove(key) != null) changedKeys += key
            }
        }

        private fun updateValues(changedKeys: MutableSet<String>) {
            updates.forEach { (key, value) ->
                if (value == null) {
                    if (values.remove(key) != null) changedKeys += key
                } else if (values[key] != value) {
                    values[key] = value
                    changedKeys += key
                }
            }
        }

        private fun notifyListeners(changedKeys: Set<String>) {
            changedKeys.forEach { key ->
                listeners.toList().forEach { listener ->
                    listener.onSharedPreferenceChanged(
                        this@InMemorySharedPreferences,
                        key,
                    )
                }
            }
        }
    }
}
