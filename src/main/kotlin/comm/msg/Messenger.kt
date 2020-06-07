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

interface Messenger<S> {

    fun hookMessageTypes(mtRegistry: MessageTypeRegistry)

    fun <M : Message<M>> send(message: M)

    fun <M : Message<M>> sendDynamic(message: (S) -> M?)

    fun receive(vararg handlers: MessageHandler<*>)

}

data class MessageHandler<M : Message<M>>(
    val type: MessageType<M>,
    val handler: (M) -> Unit
)

infix fun <M : Message<M>> MessageType<M>.handledBy(handler: (M) -> Unit) = MessageHandler(this, handler)
