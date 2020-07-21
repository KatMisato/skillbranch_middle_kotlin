package ru.skillbranch.kotlinexample

import android.annotation.SuppressLint

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        return User.makeUser(fullName = fullName, email = email, password = password).also { user ->
            if (map.containsKey(user.login)) throw IllegalArgumentException("A user with this email already exists")
            map[user.login] = user
        }
    }

    @SuppressLint("NewApi")
    fun loginUser(login: String, password: String): String? {
        println("loginUser with $login $password, norm: ${login.toNormalizedLogin()}")
        return map[login.toNormalizedLogin()]?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun requestAccessCode(login: String): Unit {
        map[login.toNormalizedLogin()]?.changeAccessCode()
    }

    fun importUsers(list: List<String>): List<User> {
        val result = ArrayList<User>()
        list.forEach { userString ->
            User.makeUser(userString).also { user ->
                result.add(user)
                if (!map.containsKey(user.login)) {
                    map[user.login] = user
                }
            }
        }
        return result
    }

    fun clearHolder() {
        map.clear()
    }

    fun registerUserByPhone(fullName: String, phone: String): User {
        if (map.filter { it.value.login == phone.toNormalizedLogin() }
                .isNotEmpty()) throw IllegalArgumentException("A user with this phone already exists")

        return User.makeUser(fullName = fullName, phone = phone).also { user ->
            map[user.login] = user
            println("registerUserByPhone, login = ${user.login}, user = $user")
        }
    }

    private fun String.toNormalizedLogin(): String {
        return replace(" ", "").replace("(", "").replace(")", "").replace("-", "").trim()
    }
}