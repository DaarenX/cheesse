## TODO

### Game
- promoting pieces
- handle game result
- show current player
- show captured pieces
- show file and rank of board
- rotate board so white is at the bottom / board rotation in general
- application icon
- resigning
- time constraints
- websocket reconnection
- close websocket when cancelling game creation
- settings screen for url config

### Server
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

### Misc
- github actions
- github pages
