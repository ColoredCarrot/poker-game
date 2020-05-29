package shared

import vendor.Swal

fun renderNextRoundPopup(label: RoundLabel) {
    Swal.Options(
        title = "Next Round: $label",
        showConfirmButton = false,
        timer = 2400
    ).fire()
}
