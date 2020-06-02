package shared

import vendor.Swal

fun renderNextRoundPopup(label: RoundLabel, underTheGun: String) {
    Swal.Options(
        title = "Next Round: $label",
        text = "$underTheGun is under the gun!",
        showConfirmButton = false,
        timer = 2400
    ).fire()
}
