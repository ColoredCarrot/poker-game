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

package sweetalert2.Swal/*
@file:JsQualifier("Swal")
@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION",
    "unused",
    "EnumEntryName", "PackageName"
)

package sweetalert2.Swal

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import kotlin.js.Promise

external fun fire(
    title: String = definedExternally,
    html: String = definedExternally,
    icon: String */
/* 'success' *//*
 = definedExternally
): Promise<sweetalert2.SweetAlertResult>

external fun fire(options: sweetalert2.SweetAlertOptions): Promise<sweetalert2.SweetAlertResult>

external fun mixin(options: sweetalert2.SweetAlertOptions = definedExternally): Any

external fun isVisible(): Boolean

external fun update(options: sweetalert2.SweetAlertOptions)

external fun close(result: sweetalert2.SweetAlertResult = definedExternally)

external fun getPopup(): HTMLElement

external fun getTitle(): HTMLElement

external fun getHeader(): HTMLElement

external fun getProgressSteps(): HTMLElement

external fun getContent(): HTMLElement

external fun getHtmlContainer(): HTMLElement

external fun getImage(): HTMLElement

external fun getCloseButton(): HTMLElement

external fun getIcon(): HTMLElement?

external fun getIcons(): Array<HTMLElement>

external fun getConfirmButton(): HTMLElement

external fun getCancelButton(): HTMLElement

external fun getActions(): HTMLElement

external fun getFooter(): HTMLElement

external fun getTimerProgressBar(): HTMLElement

external fun getFocusableElements(): Array<HTMLElement>

external fun enableButtons()

external fun disableButtons()

external fun showLoading()

external fun hideLoading()

external fun isLoading(): Boolean

external fun clickConfirm()

external fun clickCancel()

external fun showValidationMessage(validationMessage: String)

external fun resetValidationMessage()

external fun getInput(): HTMLInputElement

external fun disableInput()

external fun enableInput()

external fun getValidationMessage(): HTMLElement

external fun getTimerLeft(): Number?

external fun stopTimer(): Number?

external fun resumeTimer(): Number?

external fun toggleTimer(): Number?

external fun isTimerRunning(): Boolean?

external fun increaseTimer(n: Number): Number?

external fun queue(steps: Array<dynamic */
/* SweetAlertOptions | String *//*
>): Promise<Any>

external fun getQueueStep(): String?

external fun insertQueueStep(step: sweetalert2.SweetAlertOptions, index: Number = definedExternally): Number

external fun deleteQueueStep(index: Number)

external fun isValidParameter(paramName: String): Boolean

external fun isUpdatableParameter(paramName: String): Boolean

external fun argsToParams(params: dynamic */
/* JsTuple<Any, Any, Any> *//*
): sweetalert2.SweetAlertOptions

external fun argsToParams(params: dynamic */
/* JsTuple<SweetAlertOptions> *//*
): sweetalert2.SweetAlertOptions

external enum class DismissReason {
    cancel,
    backdrop,
    close,
    esc,
    timer
}

external var version: String
*/
