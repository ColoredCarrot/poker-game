package sweetalert2/*
//@file:JsModule("sweetalert2")
//@file:JsNonModule
@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION"
)

package sweetalert2

import org.w3c.dom.HTMLElement
import sweetalert2.Swal.DismissReason

external interface SweetAlertResult {
    var value: Any?
        get() = definedExternally
        set(value) = definedExternally
    var dismiss: DismissReason?
        get() = definedExternally
        set(value) = definedExternally
}

external interface SweetAlertShowClass {
    var popup: String?
        get() = definedExternally
        set(value) = definedExternally
    var backdrop: String?
        get() = definedExternally
        set(value) = definedExternally
    var icon: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface SweetAlertHideClass {
    var popup: String?
        get() = definedExternally
        set(value) = definedExternally
    var backdrop: String?
        get() = definedExternally
        set(value) = definedExternally
    var icon: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface SweetAlertCustomClass {
    var container: String?
        get() = definedExternally
        set(value) = definedExternally
    var popup: String?
        get() = definedExternally
        set(value) = definedExternally
    var header: String?
        get() = definedExternally
        set(value) = definedExternally
    var title: String?
        get() = definedExternally
        set(value) = definedExternally
    var closeButton: String?
        get() = definedExternally
        set(value) = definedExternally
    var icon: String?
        get() = definedExternally
        set(value) = definedExternally
    var image: String?
        get() = definedExternally
        set(value) = definedExternally
    var content: String?
        get() = definedExternally
        set(value) = definedExternally
    var input: String?
        get() = definedExternally
        set(value) = definedExternally
    var actions: String?
        get() = definedExternally
        set(value) = definedExternally
    var confirmButton: String?
        get() = definedExternally
        set(value) = definedExternally
    var cancelButton: String?
        get() = definedExternally
        set(value) = definedExternally
    var footer: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface T0 {
    @nativeGetter
    operator fun get(inputValue: String): String?

    @nativeSetter
    operator fun set(inputValue: String, value: String)
}

external interface SweetAlertOptions {
    var title: dynamic */
/* String | HTMLElement | JQuery *//*

        get() = definedExternally
        set(value) = definedExternally
    var titleText: String?
        get() = definedExternally
        set(value) = definedExternally
    var text: String?
        get() = definedExternally
        set(value) = definedExternally
    var html: dynamic */
/* String | HTMLElement | JQuery *//*

        get() = definedExternally
        set(value) = definedExternally

    */
/** actually non-nullable *//*

    var icon: String? */
/* 'success' | 'error' | 'warning' | 'info' | 'question' *//*

        get() = definedExternally
        set(value) = definedExternally
    var iconHtml: String?
        get() = definedExternally
        set(value) = definedExternally
    var footer: dynamic */
/* String | HTMLElement | JQuery *//*

        get() = definedExternally
        set(value) = definedExternally
    var backdrop: dynamic */
/* Boolean | String *//*

        get() = definedExternally
        set(value) = definedExternally
    var toast: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var target: dynamic */
/* String | HTMLElement *//*

        get() = definedExternally
        set(value) = definedExternally
    */
/** actually non-nullable *//*

    var input: String? */
/* 'text' | 'email' | 'password' | 'number' | 'tel' | 'range' | 'textarea' | 'select' | 'radio' | 'checkbox' | 'file' | 'url' *//*

        get() = definedExternally
        set(value) = definedExternally
    var width: dynamic */
/* Number | String *//*

        get() = definedExternally
        set(value) = definedExternally
    var padding: dynamic */
/* Number | String *//*

        get() = definedExternally
        set(value) = definedExternally
    var background: String?
        get() = definedExternally
        set(value) = definedExternally
    */
/** actually non-nullable *//*

    var position: String? */
/* 'top' | 'top-start' | 'top-end' | 'top-left' | 'top-right' | 'center' | 'center-start' | 'center-end' | 'center-left' | 'center-right' | 'bottom' | 'bottom-start' | 'bottom-end' | 'bottom-left' | 'bottom-right' *//*

        get() = definedExternally
        set(value) = definedExternally
    var grow: dynamic */
/* 'row' | 'column' | 'fullscreen' | Boolean *//*

        get() = definedExternally
        set(value) = definedExternally
    var showClass: SweetAlertShowClass?
        get() = definedExternally
        set(value) = definedExternally
    var hideClass: SweetAlertHideClass?
        get() = definedExternally
        set(value) = definedExternally
    var customClass: SweetAlertCustomClass?
        get() = definedExternally
        set(value) = definedExternally
    var timer: Number?
        get() = definedExternally
        set(value) = definedExternally
    var timerProgressBar: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var animation: dynamic */
/* Boolean | () -> Boolean *//*

        get() = definedExternally
        set(value) = definedExternally
    var heightAuto: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var allowOutsideClick: dynamic */
/* Boolean | () -> Boolean *//*

        get() = definedExternally
        set(value) = definedExternally
    var allowEscapeKey: dynamic */
/* Boolean | () -> Boolean *//*

        get() = definedExternally
        set(value) = definedExternally
    var allowEnterKey: dynamic */
/* Boolean | () -> Boolean *//*

        get() = definedExternally
        set(value) = definedExternally
    var stopKeydownPropagation: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var keydownListenerCapture: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var showConfirmButton: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var showCancelButton: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var confirmButtonText: String?
        get() = definedExternally
        set(value) = definedExternally
    var cancelButtonText: String?
        get() = definedExternally
        set(value) = definedExternally
    var confirmButtonColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var cancelButtonColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var confirmButtonAriaLabel: String?
        get() = definedExternally
        set(value) = definedExternally
    var cancelButtonAriaLabel: String?
        get() = definedExternally
        set(value) = definedExternally
    var buttonsStyling: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var reverseButtons: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var focusConfirm: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var focusCancel: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var showCloseButton: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeButtonHtml: String?
        get() = definedExternally
        set(value) = definedExternally
    var closeButtonAriaLabel: String?
        get() = definedExternally
        set(value) = definedExternally
    var showLoaderOnConfirm: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    val preConfirm: ((inputValue: Any) -> dynamic)?
        get() = definedExternally
    var imageUrl: String?
        get() = definedExternally
        set(value) = definedExternally
    var imageWidth: Number?
        get() = definedExternally
        set(value) = definedExternally
    var imageHeight: Number?
        get() = definedExternally
        set(value) = definedExternally
    var imageAlt: String?
        get() = definedExternally
        set(value) = definedExternally
    var inputPlaceholder: String?
        get() = definedExternally
        set(value) = definedExternally
    var inputValue: dynamic */
/* String | Promise<String> *//*

        get() = definedExternally
        set(value) = definedExternally
    var inputOptions: dynamic */
/* Map<String, String> | `T$0` | Promise<dynamic /* Map<String, String> | `T$0` *//*
> *//*

        get() = definedExternally
        set(value) = definedExternally
    var inputAutoTrim: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var inputAttributes: T0?
        get() = definedExternally
        set(value) = definedExternally
    val inputValidator: ((inputValue: String) -> dynamic)?
        get() = definedExternally
    var validationMessage: String?
        get() = definedExternally
        set(value) = definedExternally
    var progressSteps: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var currentProgressStep: String?
        get() = definedExternally
        set(value) = definedExternally
    var progressStepsDistance: String?
        get() = definedExternally
        set(value) = definedExternally
    val onBeforeOpen: ((popup: HTMLElement) -> Unit)?
        get() = definedExternally
    val onOpen: ((popup: HTMLElement) -> Unit)?
        get() = definedExternally
    val onRender: ((popup: HTMLElement) -> Unit)?
        get() = definedExternally
    val onClose: ((popup: HTMLElement) -> Unit)?
        get() = definedExternally
    val onAfterClose: (() -> Unit)?
        get() = definedExternally
    val onDestroy: (() -> Unit)?
        get() = definedExternally
    var scrollbarPadding: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}*/
*/