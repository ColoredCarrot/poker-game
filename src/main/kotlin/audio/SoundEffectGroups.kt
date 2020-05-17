package audio

import audio.SoundEffects.*
import kotlin.random.Random

enum class SoundEffectGroups(
    private vararg val sounds: SoundEffects
) {

    PLACE_CARD(PLACE_CARD_1, PLACE_CARD_2, PLACE_CARD_3),
    ;

    fun playRandom(random: Random = Random) {
        sounds.random(random).play()
    }

}
