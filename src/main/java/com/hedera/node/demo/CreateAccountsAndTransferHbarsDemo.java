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

public class CreateAndTransferHbarsDemo {

    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private CreateAndTransferHbarsDemo() {
    }

    public static void main(String[] args)
            throws TimeoutException, PrecheckStatusException, ReceiptStatusException, InterruptedException {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // First account creation
        // Generate a Ed25519 private, public key pair
        PrivateKey firstAccountKey = PrivateKey.generateED25519();
        PublicKey firstAccountPublicKey = firstAccountKey.getPublicKey();

        System.out.println("private key for first account = " + firstAccountKey);
        System.out.println("public key for first account = " + firstAccountPublicKey);

        TransactionResponse firstTransactionResponse = new AccountCreateTransaction()
                // The only _required_ property here is `key`
                .setKey(firstAccountPublicKey)
                .setInitialBalance(Hbar.fromTinybars(100_000))
                .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt firstReceipt = firstTransactionResponse.getReceipt(client);
        System.out.println("receipt for first account creation = " + firstReceipt);

        AccountId firstAccountId = firstReceipt.accountId;

        System.out.println("first account = " + firstAccountId);

        // First account creation
        // Generate a Ed25519 private, public key pair
        PrivateKey secondAccountKey = PrivateKey.generateED25519();
        PublicKey secondAccountPublicKey = secondAccountKey.getPublicKey();

        System.out.println("private key for second account = " + secondAccountKey);
        System.out.println("public key for second account = " + secondAccountPublicKey);

        TransactionResponse secondTransactionResponse = new AccountCreateTransaction()
                // The only _required_ property here is `key`
                .setKey(secondAccountPublicKey)
                .setInitialBalance(Hbar.fromTinybars(50_000))
                .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt secondReceipt = secondTransactionResponse.getReceipt(client);
        System.out.println("receipt for second account creation = " + secondReceipt);

        AccountId secondAccountId = secondReceipt.accountId;

        System.out.println("second account = " + secondAccountId);

        //Balance queries

        Hbar senderBalanceBefore = new AccountBalanceQuery()
                .setAccountId(firstAccountId)
                .execute(client)
                .hbars;

        Hbar receiptBalanceBefore = new AccountBalanceQuery()
                .setAccountId(secondAccountId)
                .execute(client)
                .hbars;

        System.out.println("" + firstAccountId + " balance = " + senderBalanceBefore);
        System.out.println("" + secondAccountId + " balance = " + receiptBalanceBefore);

        Hbar amount = Hbar.fromTinybars(100);

        Client clientForTransfer = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        clientForTransfer.setOperator(firstAccountId, firstAccountKey);

        TransactionResponse transaferTransactionResponse = new TransferTransaction()
                // .addSender and .addRecipient can be called as many times as you want as long as the total sum from
                // both sides is equivalent
                .addHbarTransfer(firstAccountId, amount.negated())
                .addHbarTransfer(secondAccountId, amount)
                .setTransactionMemo("transfer test")
                .execute(clientForTransfer);

        System.out.println("transaction ID for transfer: " + transaferTransactionResponse);

        TransactionRecord record = transaferTransactionResponse.getRecord(client);

        System.out.println("transferred " + amount + "...");

        Hbar senderBalanceAfter = new AccountBalanceQuery()
                .setAccountId(firstAccountId)
                .execute(client)
                .hbars;

        Hbar receiptBalanceAfter = new AccountBalanceQuery()
                .setAccountId(secondAccountId)
                .execute(client)
                .hbars;

        System.out.println("" + firstAccountId + " balance = " + senderBalanceAfter);
        System.out.println("" + secondAccountId + " balance = " + receiptBalanceAfter);
        System.out.println("Transfer memo: " + record.transactionMemo);
    }
}
