# poker-game

A Kotlin/JS-based browser-only poker game whose clients communicate via WebRTC.

Usage of the WebRTC protocol is abstracted by [PeerJS](https://peerjs.com/),
which also provides the default signaling server.

The app is based on React.

A live preview is available at http://home.in.tum.de/~koch/play-poker/
(*may not always reflect most recent updates*).


### Current Status

At this time, the game is under heavy development.
It is not yet feature-complete,
though it should be somewhat playable.
Breaking changes may occur at any moment.

This is just a pet project of a computer science student.
Don't expect any sort of guaranteed future support or maintenance.


### Development usage

Kotlin/JS exports all code into a `poker-game` object in the global namespace.
Some members are designed to be accessed through the browser's developer console
to expose certain operations useful during debugging.
These are described here.

The `Cheat` object contains members to view normally hidden information.
```js
// Always show all players' cards
window['poker-game'].Cheat.showEnemyCards = true
```

The object `comm.PeerJS` owns the PeerJS Server configuration data,
which may be modified freely.
Note that changes will only be reflected when (re-)connecting to the server
and should therefore be made before using any other functionality on the base site.
```js
// Switch to a localhost server configured with --port 9000 --path /peer
window['poker-game'].comm.PeerJS.switchToLocalhost()

// Individually customize configuration values
// Similar methods exist for path and port
window['poker-game'].comm.PeerJS.setCustomHost('example.com')
```
