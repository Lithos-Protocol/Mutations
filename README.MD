# Mutations
### An Ergo Library For Modular Off-Chain Code

**Mutations** is a new transaction creation library built from Ergo Appkit. It allows for modular pieces
of off-chain code called *mutations*, which may be attached to contracts or specific input utxos in order to create
output boxes according to the rules of your transaction. Mutations are reusable, flexible, and modular enough to allow
usage in large eUTXO systems. The **Mutations** API to interact with the transaction builder also allows for cleaner,
more concise Scala code compared to using default types from Ergo Appkit.