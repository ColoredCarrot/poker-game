package shared

import vendor.Swal

fun renderNextRoundPopup(label: RoundLabel, ante: Int, underTheGun: String) {
    // TODO render ante
    Swal.Options(
        title = "Next Round: $label",
        text = "$underTheGun is under the gun!",
        showConfirmButton = false,
        timer = 2400
    ).fire()
}
