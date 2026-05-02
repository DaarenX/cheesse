# TODO

## Client
- move history
  - show general history
  - allow going back
- show captured pieces
- application icon
- resigning
- time constraints
- websocket reconnection
- close websocket when cancelling game creation
- server url as datastore
- cheese icons
- show last move as highlighted squares
- show last move as animation
- dialog animations

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
- packageReleaseUberJarForCurrentOS instead of packageUberJarForCurrentOS
  - config proguard
- github actions matrix strategy
