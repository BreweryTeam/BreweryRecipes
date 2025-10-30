package dev.jsinco.recipes.core.flaws

import dev.jsinco.recipes.core.flaws.type.FlawType
import net.kyori.adventure.text.Component
import java.util.function.BiConsumer
import kotlin.random.Random

object FlawTextModificationWriter {

    fun traverse(text: Component, consumer: BiConsumer<String, Int>) {
        text.replaceText {
            var pos = 0
            it.match(".+").replacement { matchResult, _ ->
                val everything = matchResult.group()
                val prevPos = pos
                pos += everything.length
                consumer.accept(everything, matchResult.start() + prevPos)
                return@replacement Component.text(everything)
            }
        }
    }

    fun traverse(text: Component, regex: Regex, consumer: BiConsumer<String, Int>) {
        text.replaceText { it ->
            var pos = 0
            it.match(".+").replacement { matchResult, _ ->
                val everything = matchResult.group()
                regex.findAll(everything)
                    .forEach { matchResult1 ->
                        consumer.accept(matchResult1.value, matchResult1.range.start + pos)
                    }
                pos += everything.length
                return@replacement Component.text(everything)
            }
        }
    }

    fun process(
        text: Component,
        textModifications: FlawTextModifications,
        flaw: Flaw,
        offsets: Map<Int, Int>
    ): Component {
        return text.replaceText {
            var pos = 0
            val invertedOffsets = invertOffsets(offsets)
            val invalidPoints = findInvalid(offsets)
            var offset = 0
            it.match(".+").replacement { matchResult, _ ->
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
                builder.append(Component.text(modifiedText.ifEmpty { unmodifiedText }))
                return@replacement builder.build()
            }
        }
    }

    private fun findInvalid(offsets: Map<Int, Int>): Set<Int> {
        if (offsets.isEmpty()) {
            return setOf()
        }
        val output = mutableSetOf<Int>()
        var currentOffset = 0
        for (i in 0..<(offsets.keys.max() + 1)) {
            val offset = offsets[i] ?: continue
            val currentPos = i + currentOffset
            for (pos in currentPos..<(currentPos + offset)) {
                output.add(pos + 1)
            }
            currentOffset += offset
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
            for (character in string) {
                val rng = Random(config.seed + pos + character.code)
                if ((character != ' ' || overwriteSpace) && modificationFindSession.appliesTo(pos) && rng.nextDouble() < config.intensity / 100
                ) {
                    flawTextModifications.write(pos, textInfo(character.toString()), individualFlawIntensity)
                }
                pos++
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