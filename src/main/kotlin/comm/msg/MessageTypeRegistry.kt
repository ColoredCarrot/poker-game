package comm.msg

open class MessageTypeRegistry(vararg mt: Pair<String, MessageType<*>>) {

    private val keyToType = mt.toMap(HashMap())
    private val typeToKey = mt.associateTo(HashMap()) { (key, type) -> type to key }

    fun <M : Message<M>> encode(message: M): String {
        val key = typeToKey[message.type]
        return key + SEPARATOR + message.type.stringify(message)
    }

    fun decode(string: String): Message<*> {
        val (key, raw) = string.split(SEPARATOR, limit = 2)
        val mt = keyToType[key] ?: error("Cannot decode $string because there is no message type $key")
        return mt.parse(raw)
    }

    fun merge(other: MessageTypeRegistry) {
        keyToType.putAll(other.keyToType)
        typeToKey.putAll(other.typeToKey)
    }

    companion object {
        private const val SEPARATOR = " "
    }
}
