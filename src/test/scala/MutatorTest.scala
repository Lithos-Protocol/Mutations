package work.lithos

import org.ergoplatform.appkit._
import org.scalatest.funsuite.AnyFunSuite
import work.lithos.mutations.{Contract, StdMutators, TxBuilder, UTXO}

class MutatorTest extends AnyFunSuite{

  val networkType: NetworkType = NetworkType.TESTNET
  val nodePort: String = if (networkType == NetworkType.MAINNET) ":9053/" else ":9052/"

  val client = RestApiErgoClient.create(
    "http://213.239.193.208" + nodePort,
    networkType,
    "",
    RestApiErgoClient.getDefaultExplorerUrl(networkType))


  test("Standard Mutator") {
    client.execute {
      ctx =>

        val sigmaTrue = ctx.compileContract(ConstantsBuilder.empty(), " { sigmaProp(true) } ")

        val txBuilder = TxBuilder(ctx)
        val prover = ctx.newProverBuilder().withDLogSecret(BigInt(0).bigInteger).build()
        val input = UTXO(
          Contract.fromErgoContract(sigmaTrue),
          Parameters.OneErg * 5
        ).toDummyInput(ctx).withMutator(
          StdMutators.newBox(
            Contract.fromAddressString("9fq4Ha1xKGpGsg8e11wD6q7fCU4BQHYb34vqEBVQwp3cgoneEHA"),
            Parameters.OneErg,
            0
          )
        )

        val uTx = txBuilder
          .setInputs(input)
          .mutateOutputs
          .buildTx(Parameters.MinFee, Address.create("9fAMzWJa91Bdgh4a9zaHbdhjmeCsJSFCb75HFnTWV7gfTF6kDEs"))
        val sTx = prover.sign(uTx)

        println(sTx.toJson(true))
    }
  }

  test("Lambda Mutation"){
    client.execute {
      ctx =>

        val sigmaTrue = Contract.fromErgoScript(ctx, ConstantsBuilder.empty(), " { sigmaProp(true) } ")

        val txBuilder = TxBuilder(ctx)
        val prover = ctx.newProverBuilder().withDLogSecret(BigInt(0).bigInteger).build()
        val input = UTXO(
          sigmaTrue,
          Parameters.OneErg * 5
        ).toDummyInput(ctx).withMutator{
          tCtx =>
            tCtx.addOutputs(UTXO(sigmaTrue, (Parameters.OneErg * 5) - Parameters.MinFee))
        }

        val uTx = txBuilder
          .setInputs(input)
          .mutateOutputs
          .buildTx(Parameters.MinFee, Address.create("9fAMzWJa91Bdgh4a9zaHbdhjmeCsJSFCb75HFnTWV7gfTF6kDEs"))
        val sTx = prover.sign(uTx)

        println(sTx.toJson(true))
    }
  }

}
