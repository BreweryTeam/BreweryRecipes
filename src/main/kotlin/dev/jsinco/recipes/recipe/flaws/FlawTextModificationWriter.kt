package dev.jsinco.recipes.recipe.flaws

import dev.jsinco.recipes.recipe.flaws.type.FlawType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.regex.Pattern
import kotlin.random.Random

object FlawTextModificationWriter {

    val EVERYTHING: Pattern = Pattern.compile(".+")

    fun traverse(text: Component, consumer: BiConsumer<String, Int>) {
        traverse(text, 0, consumer)
    }

    private fun traverse(text: Component, startingPos: Int, consumer: BiConsumer<String, Int>): Int {
        var newPos = if (text is TextComponent) {
            consumer.accept(text.content(), startingPos)
            text.content().length + startingPos
        } else startingPos
        for (textChild in text.children()) {
            newPos = traverse(textChild, newPos, consumer)
        }
        return newPos
    }

    fun traverse(text: Component, regex: Regex, consumer: BiConsumer<String, Int>) {
        traverse(text, 0) { textString, pos ->
            regex.findAll(textString)
                .forEach { matchResult1 ->
                    consumer.accept(matchResult1.value, matchResult1.range.first + pos)
                }
        }
    }

    fun process(
        text: Component,
        textModifications: FlawTextModifications,
        flaw: Flaw,
        offsets: Map<Int, Int>,
        onModified: ((Int) -> Unit)? = null
    ): Component {
        return text.replaceText(Consumer {
            var pos = 0
            val invertedOffsets = invertOffsets(offsets)
            val invalidPoints = findInvalid(offsets)
            var offset = 0
            it.match(EVERYTHING).replacement { matchResult, _ ->
                val everything = matchResult.group()
                val prevPos = pos
                var currentPos = prevPos
                pos += everything.length
                val builder = Component.text()
                var modifiedText = ""
                var unmodifiedText = ""
                while (currentPos < pos) {
                    val currentOffsetPos = currentPos + offset
                    if (textModifications.modifies(currentOffsetPos) && !invalidPoints.contains(currentPos)) {
                        onModified?.invoke(currentOffsetPos)
                        if (modifiedText.isEmpty()) {
                            builder.append(
                                Component.text(unmodifiedText)
                            )
                            unmodifiedText = ""
                        }
                        modifiedText += textModifications.get(currentOffsetPos)!!
                    } else {
                        if (!modifiedText.isEmpty()) {
                            builder.append(
                                flaw.type.postProcess(modifiedText, currentPos, flaw.config)
                            )
                            modifiedText = ""
                        }
                        unmodifiedText += everything[currentPos - prevPos]
                    }
                    if (invertedOffsets.contains(currentPos)) {
                        offset = invertedOffsets[currentPos]!!
                    }
                    currentPos++
                }
                builder.append(
                    if (modifiedText.isEmpty()) {
                        Component.text(unmodifiedText)
                    } else {
                        flaw.type.postProcess(modifiedText, currentPos, flaw.config)
                    }
                )
                return@replacement builder.build()
            }
        })
    }


    // Positions filling out an earlier replacement that grew (not part of the original text -> don't apply flaws here)
    // The offsets are cumulative, so what a single position added is its own value minus the one before it
    private fun findInvalid(offsets: Map<Int, Int>): Set<Int> {
        if (offsets.isEmpty()) {
            return setOf()
        }
        val output = mutableSetOf<Int>()
        var previousOffset = 0
        for (i in 0..offsets.keys.max()) {
            val offset = offsets[i] ?: continue
            val grew = offset - previousOffset
            val currentPos = i + previousOffset
            for (pos in currentPos..<(currentPos + grew)) {
                output.add(pos + 1)
            }
            previousOffset = offset
        }
        return output
    }

    fun randomPositionReplacement(
        text: Component,
        modificationFindSession: FlawType.ModificationFindSession,
        individualFlawIntensity: Double,
        overwriteSpace: Boolean = false,
        textInfo: Function1<String, String>
    ): FlawTextModifications {
        val flawTextModifications = FlawTextModifications()
        val config = modificationFindSession.config
        if (config.intensity == 0.0) {
            return flawTextModifications
        }
        traverse(text) { string, startPos ->
            var pos = startPos
            var index = 0
            while (index < string.length) {
                // Emojis can be two chars -> flaw them together
                val codePoint = string.codePointAt(index)
                val charCount = Character.charCount(codePoint)
                val sign = string.substring(index, index + charCount)
                val random = Random(config.seed + pos + codePoint)
                if ((sign != " " || overwriteSpace) &&
                    modificationFindSession.appliesTo(pos) && random.nextDouble() < config.intensity / 100
                ) {
                    flawTextModifications.write(pos, textInfo(sign), individualFlawIntensity)
                    for (trailing in 1..<charCount) {
                        flawTextModifications.write(pos + trailing, "", 0.0)
                    }
                }
                pos += charCount
                index += charCount
            }
        }
        return flawTextModifications
    }

    private fun invertOffsets(offsets: Map<Int, Int>): Map<Int, Int> {
        val output = mutableMapOf<Int, Int>()
        for (entry in offsets) {
            output[entry.key + entry.value] = -entry.value
        }
        return output
    }
}