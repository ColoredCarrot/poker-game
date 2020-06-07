/*
 * Copyright 2020 Julian Koch and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
