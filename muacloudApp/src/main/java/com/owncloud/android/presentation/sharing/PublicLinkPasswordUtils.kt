

package com.owncloud.android.presentation.sharing

import java.security.SecureRandom

private val charsetLowercase = ('a'..'z').toList()
private val charsetUppercase = ('A'..'Z').toList()
private val charsetDigits = ('0'..'9').toList()
private val charsetSpecial = listOf('!', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~')

fun generatePassword(
    minChars: Int?,
    maxChars: Int?,
    minDigitsChars: Int?,
    minLowercaseChars: Int?,
    minUppercaseChars: Int?,
    minSpecialChars: Int?,
): String {

    val minCharacters = if (minChars == null || minChars == 0) 8 else minChars
    val maxCharacters = if (maxChars == null || maxChars == 0) 72 else maxChars
    val minDigits = if (minDigitsChars == null || minDigitsChars == 0) 1 else minDigitsChars
    val minLowercaseCharacters = if (minLowercaseChars == null || minLowercaseChars == 0) 1 else minLowercaseChars
    val minUppercaseCharacters = if (minUppercaseChars == null || minUppercaseChars == 0) 1 else minUppercaseChars
    val minSpecialCharacters = if (minSpecialChars == null || minSpecialChars == 0) 1 else minSpecialChars

    val secureRandom = SecureRandom()

    val length = secureRandom.nextInt(maxCharacters - minCharacters + 1) + minCharacters

    val passwordChars = mutableListOf<Char>()

    for (i in 1..minDigits) {
        passwordChars.add(charsetDigits[secureRandom.nextInt(charsetDigits.size)])
    }

    for (i in 1..minLowercaseCharacters) {
        passwordChars.add(charsetLowercase[secureRandom.nextInt(charsetLowercase.size)])
    }

    for (i in 1..minUppercaseCharacters) {
        passwordChars.add(charsetUppercase[secureRandom.nextInt(charsetUppercase.size)])
    }

    for (i in 1..minSpecialCharacters) {
        passwordChars.add(charsetSpecial[secureRandom.nextInt(charsetSpecial.size)])
    }

    val allCharsets = charsetLowercase + charsetUppercase + charsetDigits + charsetSpecial
    while (passwordChars.size < length) {
        passwordChars.add(allCharsets[secureRandom.nextInt(allCharsets.size)])
    }

    passwordChars.shuffle(secureRandom)

    return passwordChars.joinToString("")
}
