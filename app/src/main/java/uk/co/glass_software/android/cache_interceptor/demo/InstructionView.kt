package uk.co.glass_software.android.cache_interceptor.demo

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.widget.TextView
import uk.co.glass_software.android.cache_interceptor.configuration.CacheInstruction
import uk.co.glass_software.android.cache_interceptor.configuration.CacheInstruction.Operation
import uk.co.glass_software.android.cache_interceptor.configuration.CacheInstruction.Operation.Type.*

class InstructionView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0)
    : TextView(context, attrs, defStyleAttr) {

    private val orange = Color.parseColor("#CC7832")
    private val purple = Color.parseColor("#9876AA")
    private val yellow = Color.parseColor("#FFC66D")
    private val green = Color.parseColor("#629755")
    private val blue = Color.parseColor("#467CDA")
    private val white = Color.parseColor("#A9B7C6")

    //TODO add colour
    fun setInstruction(instruction: CacheInstruction) {
        text = instruction.operation.type.annotationName.let {
            TextUtils.concat(
                    getRestMethod(instruction.operation),
                    applyOperationStyle(it),
                    getDirectives(it.length + 1, instruction.operation),
                    getMethod(instruction)
            )
        }
    }

    private fun getRestMethod(operation: Operation): CharSequence =
            when (operation.type) {
                DO_NOT_CACHE,
                CACHE,
                REFRESH,
                OFFLINE -> "@GET(\"fact\")"

                INVALIDATE,
                CLEAR,
                CLEAR_ALL -> "@DELETE(\"fact\")"
            }.let {
                applyAnnotationStyle(it, true)
            }

    private fun getDirectives(length: Int,
                              operation: Operation): CharSequence =
            "".padStart(length, ' ')
                    .let { padding ->
                        when (operation.type) {
                            CACHE -> (operation as Operation.Expiring.Cache).let { cacheOperation ->
                                arrayOf(
                                        "freshOnly = ${cacheOperation.freshOnly}",
                                        "durationInMillis = ${cacheOperation.durationInMillis
                                                ?: "-1L"}",
                                        "mergeOnNextOnError = ${getOptionalBoolean(cacheOperation.mergeOnNextOnError)}",
                                        "encrypt = ${getOptionalBoolean(cacheOperation.encrypt)}",
                                        "compress = ${getOptionalBoolean(cacheOperation.compress)}"
                                )
                            }

                            REFRESH -> (operation as Operation.Expiring.Refresh).let { refreshOperation ->
                                arrayOf(
                                        "freshOnly = ${refreshOperation.freshOnly}",
                                        "durationInMillis = ${refreshOperation.durationInMillis
                                                ?: "-1L"}",
                                        "mergeOnNextOnError = ${getOptionalBoolean(refreshOperation.mergeOnNextOnError)}"
                                )
                            }

                            OFFLINE -> (operation as Operation.Expiring.Offline).let { offlineOperation ->
                                arrayOf(
                                        "freshOnly = ${offlineOperation.freshOnly}",
                                        "mergeOnNextOnError = ${getOptionalBoolean(offlineOperation.mergeOnNextOnError)}"
                                )
                            }

                            CLEAR,
                            CLEAR_ALL -> arrayOf(
                                    "clearOldEntriesOnly = ${(operation as Operation.Clear).clearOldEntriesOnly}"
                            )

                            INVALIDATE,
                            DO_NOT_CACHE -> emptyArray()
                        }.let { directives ->
                            directives.mapIndexed { index, directive ->
                                applyDirectiveStyle(
                                        (if (index == 0) "" else padding)
                                                .plus(directive)
                                                .plus((if (index != directives.size - 1) ",\n" else ""))
                                )
                            }
                        }.let {
                            val array = arrayOfNulls<CharSequence>(it.size)
                            it.forEachIndexed { index, charSequence ->
                                array[index] = charSequence
                            }
                            TextUtils.concat("(", TextUtils.concat(*array), ")")
                        }
                    }.let {
                        if (it.isEmpty()) it
                        else applyAnnotationStyle(it, false)
                    }

    private fun applyAnnotationStyle(it: CharSequence,
                                     colourParameters: Boolean): SpannableString {
        val leftBracket = it.indexOf('(')
        val rightBracket = it.indexOf(')')

        return SpannableString(it).apply {
            setSpan(
                    ForegroundColorSpan(orange),
                    0,
                    leftBracket,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                    ForegroundColorSpan(white),
                    leftBracket,
                    leftBracket + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (colourParameters) {
                setSpan(
                        ForegroundColorSpan(green),
                        leftBracket + 1,
                        rightBracket,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            setSpan(
                    ForegroundColorSpan(white),
                    rightBracket,
                    rightBracket + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    leftBracket,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun applyOperationStyle(operation: String): SpannableString {
        return SpannableString("\n$operation").apply {
            setSpan(
                    ForegroundColorSpan(orange),
                    0,
                    operation.length + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    operation.length + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun applyDirectiveStyle(directive: CharSequence): CharSequence {
        val equal = directive.indexOf('=')
        return SpannableString(directive).apply {
            setSpan(
                    ForegroundColorSpan(blue),
                    0,
                    equal + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                    ForegroundColorSpan(purple),
                    equal + 1,
                    directive.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun getOptionalBoolean(optional: Boolean?) =
            "OptionalBoolean.".plus(
                    when {
                        optional == null -> "DEFAULT"
                        optional -> "TRUE"
                        else -> "FALSE"
                    }
            )

    private fun getMethod(instruction: CacheInstruction): CharSequence =
            ("\nfun call(): " + when (instruction.operation.type) {
                CACHE,
                REFRESH -> "Observable<${instruction.responseClass.simpleName}>"

                DO_NOT_CACHE,
                OFFLINE -> "Single<${instruction.responseClass.simpleName}>"

                INVALIDATE,
                CLEAR,
                CLEAR_ALL -> "Completable"
            }).let {
                val leftBracket = it.indexOf('(')
                SpannableString(it).apply {
                    setSpan(
                            ForegroundColorSpan(orange),
                            0,
                            4,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                            ForegroundColorSpan(yellow),
                            4,
                            leftBracket,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            leftBracket,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
}