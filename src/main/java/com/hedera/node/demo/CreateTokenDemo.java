package com.hedera.node.demo;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenInfo;
import com.hedera.hashgraph.sdk.TokenInfoQuery;
import com.hedera.hashgraph.sdk.TokenSupplyType;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import lombok.CustomLog;

@CustomLog
public class CreateTokenDemo {

    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private CreateTokenDemo(){}

    public static void main(String[] args)
            throws InterruptedException, PrecheckStatusException, TimeoutException, ReceiptStatusException {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        //Create an account
        PrivateKey aliceKey = PrivateKey.generateED25519();
        AccountId aliceId = new AccountCreateTransaction()
                .setInitialBalance(new Hbar(10))
                .setKey(aliceKey)
                .freezeWith(client)
                .sign(aliceKey)
                .execute(client)
                .getReceipt(client)
                .accountId;

        //Create a fungible token
        TokenId tokenId = new TokenCreateTransaction()
                .setTokenType(TokenType.FUNGIBLE_COMMON)
                .setTokenName("Example Token")
                .setTokenSymbol("EX")
                .setAdminKey(aliceKey)
                .setSupplyKey(aliceKey)
                .setFeeScheduleKey(aliceKey)
                .setWipeKey(aliceKey)
                .setTreasuryAccountId(aliceId)
                .setInitialSupply(100)
                .freezeWith(client)
                .sign(aliceKey)
                .execute(client)
                .getReceipt(client)
                .tokenId;

        log.info("Created token with ID {}", tokenId);

        TokenInfo fungibleTokenInfo = new TokenInfoQuery()
                .setTokenId(tokenId)
                .execute(client);

        log.info("Token info for fungible token is: {}", fungibleTokenInfo);


        // Create an NFT
        TransactionReceipt nftCreateReceipt = new TokenCreateTransaction()
                .setTokenName("NFT Token")
                .setTokenSymbol("NFT")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setDecimals(0)
                .setInitialSupply(0)
                .setMaxSupply(1000L)
                .setTreasuryAccountId(OPERATOR_ID)
                .setSupplyType(TokenSupplyType.FINITE)
                .setAdminKey(OPERATOR_KEY)
                .setSupplyKey(OPERATOR_KEY)
                .freezeWith(client)
                .execute(client)
                .getReceipt(client);

        TokenId nftTokenId = nftCreateReceipt.tokenId;
        log.info("Created NFT with token ID: {}", nftTokenId);

        TokenInfo nftTokenInfo = new TokenInfoQuery()
                .setTokenId(nftTokenId)
                .execute(client);

        log.info("Token info for NFT is: {}", nftTokenInfo);
    }
}
