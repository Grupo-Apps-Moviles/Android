package es.upc.waypass.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs =
        context.getSharedPreferences(
            "auth_prefs",
            Context.MODE_PRIVATE
        )

    // TOKEN

    fun saveToken(token: String) {
        prefs.edit().putString("jwt", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt", null)
    }

    // USER ID

    fun saveUserId(userId: Int) {
        prefs.edit().putInt("user_id", userId).apply()
    }

    fun getUserId(): Int? {
        val value = prefs.getInt("user_id", -1)
        return if (value == -1) null else value
    }

    // ROLE

    fun saveRole(role: Int) {
        prefs.edit().putInt("role", role).apply()
    }

    fun getRole(): Int? {
        val value = prefs.getInt("role", -1)
        return if (value == -1) null else value
    }

    // LOGOUT

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}