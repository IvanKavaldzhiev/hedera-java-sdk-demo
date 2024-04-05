package com.hedera.node.demo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import lombok.CustomLog;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.core.io.DefaultResourceLoader;

@CustomLog
public class ContractDeployAndCallDemo {

    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private ContractDeployAndCallDemo() {
    }

    @SneakyThrows
    public static void main(String[] args) {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        final var resourceLoader = new DefaultResourceLoader();
        var resource = resourceLoader.getResource("classpath:Parent.json");
        try (var in = resource.getInputStream()) {
            CompiledSolidityArtifact compiledSolidityArtifact = readCompiledArtifact(in);

            FileCreateTransaction fileCreateTx = new FileCreateTransaction()
                    .setKeys(OPERATOR_KEY)
                    //Set the bytecode of the contract
                    .setContents(new byte[]{});

            //Submit the file to the Hedera test network signing with the transaction fee payer key specified with the client
            TransactionResponse submitTx = fileCreateTx.execute(client);

            //Get the receipt of the file create transaction
            TransactionReceipt fileReceipt = submitTx.getReceipt(client);

            //Get the file ID from the receipt
            FileId bytecodeFileId = fileReceipt.fileId;

            //Log the file ID
            log.info("The smart contract bytecode file ID is {}", bytecodeFileId);

            FileAppendTransaction fileAppendTransaction = new FileAppendTransaction().setFileId(bytecodeFileId)
                    .setContents(compiledSolidityArtifact.bytecode.replaceFirst("0x", "").getBytes(StandardCharsets.UTF_8))
                    .setTransactionMemo("Append bytecode contents")
                    .setChunkSize(4096)
                            .freezeWith(client).sign(OPERATOR_KEY);

            fileAppendTransaction.execute(client);

            // Instantiate the contract instance
            ContractCreateTransaction contractTx = new ContractCreateTransaction()
                //Set the file ID of the Hedera file storing the bytecode
                .setBytecodeFileId(bytecodeFileId)
                //Set the gas to instantiate the contract
                .setGas(4_000_000);

            //Submit the transaction to the Hedera test network
            TransactionResponse contractResponse = contractTx.execute(client);

            //Get the receipt of the file create transaction
            TransactionReceipt contractReceipt = contractResponse.getReceipt(client);

            //Get the smart contract ID
            ContractId newContractId = contractReceipt.contractId;

            //Log the smart contract ID
            log.info("The smart contract ID is {}", newContractId);

            //Perform call

            ContractExecuteTransaction contractExecuteTransaction = new ContractExecuteTransaction()
                    .setContractId(newContractId)
                    .setGas(1_000_000L)
                    .setTransactionMemo("Call a contract")
                    .setFunction("getBytecode");

            final var callResponse = contractExecuteTransaction.execute(client);
            final var callReceipt = callResponse.getReceipt(client);

            log.info("The smart contract call transaction ID is {}", callReceipt.transactionId);
        } catch (ReceiptStatusException | TimeoutException | PrecheckStatusException e) {
            throw new RuntimeException(e);
        }
    }

    private static CompiledSolidityArtifact readCompiledArtifact(InputStream in) throws IOException {
            final var mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.readValue(in, CompiledSolidityArtifact.class);
    }

    @Data
    private static class CompiledSolidityArtifact {
        private Object[] abi;
        private String bytecode;
    }
}
