package com.hedera.node.demo;

import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionRecord;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import lombok.CustomLog;

@CustomLog
public class CreateAccountsAndTransferHbarsDemo {

    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private CreateAccountsAndTransferHbarsDemo() {
    }

    public static void main(String[] args)
            throws TimeoutException, PrecheckStatusException, ReceiptStatusException, InterruptedException {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // First account creation
        // Generate an ED25519 private, public key pair
        PrivateKey firstAccountKey = PrivateKey.generateED25519();
        PublicKey firstAccountPublicKey = firstAccountKey.getPublicKey();

        log.info("Private key for sender account = {}", firstAccountKey);
        log.info("Public key for sender account = {}", firstAccountPublicKey);

        TransactionResponse firstTransactionResponse = new AccountCreateTransaction()
                // The only _required_ property here is `key`
                .setKey(firstAccountPublicKey)
                .setInitialBalance(Hbar.fromTinybars(100_000))
                .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt firstReceipt = firstTransactionResponse.getReceipt(client);
        log.info("Receipt for sender account creation = {}", firstReceipt);

        AccountId firstAccountId = firstReceipt.accountId;

        log.info("Sender account = {}", firstAccountId);

        // Second account creation
        // Generate an ED25519 private, public key pair
        PrivateKey secondAccountKey = PrivateKey.generateED25519();
        PublicKey secondAccountPublicKey = secondAccountKey.getPublicKey();

        log.info("Private key for recipient account = {}", secondAccountKey);
        log.info("Public key for recipient account = {}", secondAccountPublicKey);

        TransactionResponse secondTransactionResponse = new AccountCreateTransaction()
                // The only _required_ property here is `key`
                .setKey(secondAccountPublicKey)
                .setInitialBalance(Hbar.fromTinybars(50_000))
                .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt secondReceipt = secondTransactionResponse.getReceipt(client);
        log.info("Receipt for recipient account creation = {}", secondReceipt);

        AccountId secondAccountId = secondReceipt.accountId;

        log.info("Recipient accountId = {}", secondAccountId);

        //Balance queries

        Hbar senderBalanceBefore = new AccountBalanceQuery()
                .setAccountId(firstAccountId)
                .execute(client)
                .hbars;

        Hbar receiptBalanceBefore = new AccountBalanceQuery()
                .setAccountId(secondAccountId)
                .execute(client)
                .hbars;

        log.info("Sender with id {} has a balance before transfer = {}", firstAccountId, senderBalanceBefore);
        log.info("Recipient with id {} has a balance before transfer = {}", secondAccountId, receiptBalanceBefore);

        Hbar amount = Hbar.fromTinybars(100);

        Client clientForTransfer = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        clientForTransfer.setOperator(firstAccountId, firstAccountKey);

        TransactionResponse transferTransactionResponse = new TransferTransaction()
                // .addSender and .addRecipient can be called as many times as you want as long as the total sum from
                // both sides is equivalent
                .addHbarTransfer(firstAccountId, amount.negated())
                .addHbarTransfer(secondAccountId, amount)
                .setTransactionMemo("transfer test")
                .execute(clientForTransfer);

        log.info("Transaction ID for transfer: {}", transferTransactionResponse);

        TransactionRecord record = transferTransactionResponse.getRecord(client);

        log.info("Transferred {} tinybars", amount);

        Hbar senderBalanceAfter = new AccountBalanceQuery()
                .setAccountId(firstAccountId)
                .execute(client)
                .hbars;

        Hbar receiptBalanceAfter = new AccountBalanceQuery()
                .setAccountId(secondAccountId)
                .execute(client)
                .hbars;

        log.info("Sender account with id {} has a balance after transfer = {}", firstAccountId, senderBalanceAfter);
        log.info("Recipient account with id {} has a balance after transfer = {}", secondAccountId, receiptBalanceAfter);
        log.info("Transfer memo: {}", record.transactionMemo);
    }
}
