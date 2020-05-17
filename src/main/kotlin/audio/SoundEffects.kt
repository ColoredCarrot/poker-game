package audio

import org.w3c.dom.Audio

enum class SoundEffects(name: String) {

    PLACE_CARD_1("card_place_1"),
    PLACE_CARD_2("card_place_2"),
    PLACE_CARD_3("card_place_3"),

    WIN("win", true),
    APPLAUSE("applause", true),
    ;

    constructor(name: String, preload: Boolean) : this(name) {
        if (preload) audio
    }

    private val audio by lazy {
        Audio("sound/$name.mp3")
    }

    fun play() {
        audio.play()
    }

}
