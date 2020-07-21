package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {
    val userInfo: String

    private val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").capitalize()

    private val initials: String
        get() = listOfNotNull(firstName, lastName).map { it.first().toUpperCase() }
            .joinToString(" ").capitalize()

    private var phone: String? = null
        set(value) {
            field = value?.replace("[^+\\d]".toRegex(), "")
        }

    private var _login: String? = null
    internal var login: String
        set(value) {
            _login = value?.toLowerCase()
        }
        get() = _login!!


    private var _salt: String? = null
    private val salt: String by lazy {
        if (_salt == null) ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
        else _salt.toString()
    }

    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        password: String
    ) : this(firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("Secondary mail constructor")
        passwordHash = encrypt(password)
    }

    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String?
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("Secondary phone constructor")
        changeAccessCode()
        accessCode?.let { sendAccessCodeToUser(phone, it) }
    }

    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        rawPhone: String?,
        salt: String,
        passwordHash: String
    ) : this(
        firstName,
        lastName,
        email = email,
        rawPhone = rawPhone,
        meta = mapOf("src" to "csv")
    ) {
        println("Csv email constructor")
        this._salt = salt
        this.passwordHash = passwordHash
        changeAccessCode()
    }

    private fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }

    init {
        println("First init block, primary constructor was called")

        check(!firstName.isBlank()) { "FirstName must be not blank" }
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) { "Email or phone must be not blank" }

        phone =
            rawPhone?.replace(" ", "")?.replace("(", "")?.replace(")", "")?.replace("-", "")?.trim()
                ?: rawPhone
        if (email.isNullOrBlank()) {
            checkPhone(
                phone!!
            )
        }
        login = email ?: phone!!

        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
            """.trimIndent()

        println("First init block, userInfo = $userInfo")
    }

    fun checkPassword(pass: String): Boolean {
        println("checkPassword encrypt(pass) ${encrypt(pass)} passwordHash = $passwordHash, salt = $salt, _salt = $_salt")
        return encrypt(pass) == passwordHash
    }

    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) passwordHash = encrypt(newPass)
        else throw IllegalAccessException("The entered password does not match the current password")
    }

    fun changeAccessCode(): Unit {
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
    }

    private fun encrypt(password: String): String = salt.plus(password).md5()

    private fun sendAccessCodeToUser(phone: String?, code: String) {
        println("..... sending access code: $code on $phone")
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()
            return when {
                !phone.isNullOrBlank() -> {
                    User(
                        firstName,
                        lastName,
                        phone
                    )
                }
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(
                    firstName,
                    lastName,
                    email,
                    password
                )
                else -> throw IllegalArgumentException("Email or phone must be not null or blank")
            }
        }

        fun makeUser(
            csvString: String
        ): User {
            println("Import user from string $csvString")

            val fields = csvString.split(";")

            val (firstName, lastName) = fields.first().fullNameToPair()
            val email = fields.getOrNull(1)
            val (salt, passwordHash) = fields.getOrNull(2)?.saltHashToPair()
                ?: throw IllegalArgumentException("Salt and passwordHash must be not null or blank")
            val phone = fields.getOrNull(3)

            println(
                """Import user from $csvString
                    firstName = $firstName
                    lastName = $lastName
                    email = $email
                    salt = $salt
                    passwordHash = $passwordHash
                    phone = $phone
                    """
            )

            return User(
                firstName = firstName,
                lastName = lastName,
                rawPhone = phone,
                email = email,
                salt = salt,
                passwordHash = passwordHash
            )
        }

        private fun checkPhone(phone: String): Boolean {
            return when {
                """^\+\d((\d{3})|(\(\d{3}\)))\d{3}[-]?\d{2}[-]?\d{2}$""".toRegex()
                    .containsMatchIn(phone) -> true
                else -> throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
            }
        }

        private fun String.fullNameToPair(): Pair<String, String?> {
            return split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException(
                            "Full name must contain only first name and last name, " +
                                    "current split result ${this@fullNameToPair}"
                        )
                    }
                }
        }

        private fun String.saltHashToPair(): Pair<String, String> {
            return split(":")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        2 -> first() to last()
                        else -> throw IllegalArgumentException(
                            "Full name must contain only first name and last name, " +
                                    "current split result ${this@saltHashToPair}"
                        )
                    }
                }
        }
    }
}