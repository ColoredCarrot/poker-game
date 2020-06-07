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

import COMM_JSON
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class JsonMessage<D>(
    val data: D,
    type: Type<D>
) : AbstractMessage<JsonMessage<D>>(type) {

    constructor(data: D, serializer: KSerializer<D>, json: Json = DEFAULT_JSON) : this(data, Type<D>(serializer, json))

    operator fun component1() = data

    open class Type<D>(
        private val serializer: KSerializer<D>,
        private val json: Json = DEFAULT_JSON
    ) : MessageType<JsonMessage<D>> {

        override fun parse(raw: String): JsonMessage<D> {
            return JsonMessage(json.parse(serializer, raw), this)
        }

        override fun stringify(m: JsonMessage<D>): String {
            return json.stringify(serializer, m.data)
        }

    }

    companion object {
        private val DEFAULT_JSON = COMM_JSON
    }
}

fun <D> MessageTypeRegistry.encode(data: D, jsonType: JsonMessage.Type<D>) = encode(JsonMessage(data, jsonType))

fun <D : JsonMessageToken<D>> Messenger<*>.send(data: D) = send(JsonMessage(data, data.jsonType))

interface JsonMessageToken<D : JsonMessageToken<D>> {
    val jsonType: JsonMessage.Type<D>
}

fun <D : JsonMessageToken<D>> D.jsonMessage()  = JsonMessage(this, jsonType)
