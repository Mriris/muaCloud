

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
    // If params are null or 0 (N/A), they are set a value by default
    val minCharacters = if (minChars == null || minChars == 0) 8 else minChars
    val maxCharacters = if (maxChars == null || maxChars == 0) 72 else maxChars
    val minDigits = if (minDigitsChars == null || minDigitsChars == 0) 1 else minDigitsChars
    val minLowercaseCharacters = if (minLowercaseChars == null || minLowercaseChars == 0) 1 else minLowercaseChars
    val minUppercaseCharacters = if (minUppercaseChars == null || minUppercaseChars == 0) 1 else minUppercaseChars
    val minSpecialCharacters = if (minSpecialChars == null || minSpecialChars == 0) 1 else minSpecialChars

    val secureRandom = SecureRandom()

    // Determine the number of characters to generate randomly within the provided range
    val length = secureRandom.nextInt(maxCharacters - minCharacters + 1) + minCharacters

    // First, store all the chars that will be part of the password unordered
    val passwordChars = mutableListOf<Char>()

    // Include the minimum number of digits established by the policy
    for (i in 1..minDigits) {
        passwordChars.add(charsetDigits[secureRandom.nextInt(charsetDigits.size)])
    }

    // Include the minimum number of lowercase chars established by the policy
    for (i in 1..minLowercaseCharacters) {
        passwordChars.add(charsetLowercase[secureRandom.nextInt(charsetLowercase.size)])
    }

    // Include the minimum number of uppercase chars established by the policy
    for (i in 1..minUppercaseCharacters) {
        passwordChars.add(charsetUppercase[secureRandom.nextInt(charsetUppercase.size)])
    }

    // Include the minimum number of special chars established by the policy
    for (i in 1..minSpecialCharacters) {
        passwordChars.add(charsetSpecial[secureRandom.nextInt(charsetSpecial.size)])
    }

    // Fill with random characters from every charset until determined length is reached
    val allCharsets = charsetLowercase + charsetUppercase + charsetDigits + charsetSpecial
    while (passwordChars.size < length) {
        passwordChars.add(allCharsets[secureRandom.nextInt(allCharsets.size)])
    }

    // Shuffle chars in the unordered list and convert it to String
    passwordChars.shuffle(secureRandom)

    return passwordChars.joinToString("")
}
