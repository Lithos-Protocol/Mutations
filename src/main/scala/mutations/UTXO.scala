package work.lithos
package mutations

import org.ergoplatform.appkit.{BlockchainContext, ErgoId, ErgoValue, OutBox}

import scala.util.{Failure, Success, Try}

case class UTXO(contract: Contract, value: Long,
                tokens: Seq[Token] = Seq.empty[Token],
                registers: Seq[ErgoValue[_]] = Seq.empty[ErgoValue[_]]) {

  def setTokens(tokens: Token*): UTXO = this.copy(tokens = tokens)

  def setRegs(regs: ErgoValue[_]*): UTXO = this.copy(registers = regs)

  def withReg(reg: ErgoValue[_], idx: Int): UTXO = {
    setRegs(registers.patch(idx, Seq(reg), 1): _*)
  }

  def setValue(value: Long): UTXO = this.copy(value = value)

  def setContract(contract: Contract): UTXO = this.copy(contract = contract)

  def addToken(token: Token): UTXO = setTokens(tokens ++ Seq(token) :_* )

  def removeToken(token: Token): UTXO = {
    val tokenIdx = tokens.indexWhere(t => t.id.toString == token.id.toString)
    val optToken: Option[Token] = if(tokenIdx != -1) Some(tokens(tokenIdx)) else None
    optToken match {
      case Some(value) =>
        val trySub = Try(value - token.amount)

        trySub match {
          case Failure(exception) => setTokens(tokens.patch(tokenIdx, Seq(), 1): _*)
          case Success(value) => setTokens(tokens.patch(tokenIdx, Seq(value), 1): _*)
        }
      case None =>
        throw new Exception("Tried to subtract non existent token!")
    }
  }

  def toOutBox(ctx: BlockchainContext): OutBox = {
    var out = ctx.newTxBuilder().outBoxBuilder()
      .value(value)
      .contract(contract.ergoContract(ctx))

    if(tokens.nonEmpty)
      out = out.tokens(tokens.map(_.toErgo): _*)

    if(registers.nonEmpty)
      out = out.registers(registers: _*)

    out.build()
  }

  def toInput(ctx: BlockchainContext, txId: ErgoId, outIdx: Short): InputUTXO = {
    InputUTXO(toOutBox(ctx).convertToInputWith(txId.toString, outIdx))
  }

  def toDummyInput(ctx: BlockchainContext): InputUTXO = {
    toInput(ctx, ErgoId.create("a1086e447695dc8dcb79c0bf3b06ed715ccfa2b28ef44889ebfbda16c00ff34b"), 0.toShort)
  }

}

object UTXO {
  def apply(contract: Contract, value: Long,
            tokens: Seq[Token] = Seq.empty[Token],
            registers: Seq[ErgoValue[_]] = Seq.empty[ErgoValue[_]]): UTXO = {
    new UTXO(contract, value, tokens, registers)
  }
}
