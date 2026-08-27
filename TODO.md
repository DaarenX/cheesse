# TODO

## Client
### Functional
- move history
  - show general history
  - allow going back
- show captured pieces
- resigning
- time constraints
- websocket reconnection
- close websocket when cancelling game creation
- server url as datastore
- fix (mac) trackpad drag bug

### Design
- show last move as highlighted squares
- show last move as animation
- dialog animations
- move preview highlighting
- making moves without dragging
- replace shadow / glow with outlined icon
- custom chess piece icons

## Server
- encapsule chess engine in chessengineadapter
- global exception handler and custom exceptions
- resigning
- time constraints
- a lot of tests
- remove game sessions after some time
- draw-by-3-fold tracking (store and load move history instead of only fen)
- websocket disconnection handling
- redis for lobby creation
- game status in db
- lobby shareable link

## Misc
- fix xcode compose multiplatform 1.11.0 update
- signing ios
- signing android
- packageReleaseUberJarForCurrentOS instead of packageUberJarForCurrentOS
      - config proguard
- github actions matrix strategy
- windows icon (lol)
- linux app icon
- ios app icon
- github description banner
