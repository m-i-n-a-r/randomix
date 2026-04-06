package com.minar.randomix.utilities

object RecentUtils {

    fun toOptionList(formattedOptions: String): List<String> =
        formattedOptions.trim().split(" | ")

    fun fromOptionList(optionList: List<String>): String {
        val copy = optionList.toMutableList().also { it.remove(Constants.PIN_WORKAROUND_ENTRY) }
        return copy.joinToString(" | ")
    }
}
