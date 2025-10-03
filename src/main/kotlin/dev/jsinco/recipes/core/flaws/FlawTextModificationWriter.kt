package dev.jsinco.recipes.core.flaws

import net.kyori.adventure.text.Component
import java.util.function.BiConsumer
import java.util.function.Predicate
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
                val prevPos = pos
                pos += everything.length
                regex.findAll(everything)
                    .forEach { matchResult1 ->
                        consumer.accept(matchResult1.value, matchResult1.range.start + pos)
                    }
                consumer.accept(everything, matchResult.start() + prevPos)
                return@replacement Component.text(everything)
            }
        }
    }

    fun process(
        text: Component,
        textModifications: FlawTextModifications,
        flawType: FlawType,
        seed: Int,
        offset: Map<Int, Int>
    ): Component {
        return text.replaceText {
            var pos = 0
            it.match(".+").replacement { matchResult, _ ->
                val everything = matchResult.group()
                val prevPos = pos
                pos += everything.length
                var currentPos = pos
                val builder = Component.text()
                var modifiedText = ""
                var unmodifiedText = ""
                while (currentPos < everything.length) {
                    val offsetPos = currentPos - (offset[offset.filter { offsetPos -> offsetPos.key < currentPos }
                        .maxOf { offsetPos -> offsetPos.key }] ?: 0)
                    if (textModifications.contains(currentPos)) {
                        if (modifiedText.isEmpty()) {
                            builder.append(
                                Component.text(unmodifiedText)
                            )
                            unmodifiedText = ""
                        }
                        modifiedText += textModifications.get(currentPos)!!
                    } else {
                        if (!modifiedText.isEmpty()) {
                            builder.append(
                                flawType.postProcess(modifiedText, matchResult.start() + prevPos, seed)
                            )
                            modifiedText = ""
                        }
                        unmodifiedText += everything[currentPos]
                    }
                    currentPos++
                }
                builder.append(Component.text(modifiedText.ifEmpty { unmodifiedText }))
                return@replacement builder.build()
            }
        }
    }

    fun randomPositionReplacement(
        text: Component,
        config: FlawConfig,
        individualFlawIntensity: Double,
        filter: Predicate<Int>,
        textInfo: Function1<String, String>
    ): FlawTextModifications {
        val flawTextModifications = FlawTextModifications()
        if (config.intensity == 0.0) {
            return flawTextModifications
        }
        traverse(text) { string, startPos ->
            var pos = startPos
            for (character in string) {
                val rng = Random(config.seed + pos + character.code)
                if (character != ' ' && config.extent.appliesTo(pos) && rng.nextDouble() < config.intensity / 100 && filter.test(
                        pos
                    )
                ) {
                    flawTextModifications.write(pos, textInfo(character.toString()), individualFlawIntensity)
                }
                pos++
            }
            flawTextModifications.totalLength = startPos + string.length
        }
        return flawTextModifications
    }
}