package hi.sejun.infuraex1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;

import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    Web3j web3j;
    Credentials credentials;
    Admin admin;
    final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        web3j = Web3j.build(new HttpService("https://ropsten.infura.io/v3/1b4540028fcd48cfa8d2167e17cf3db1"));
        admin = Admin.build(new HttpService("https://ropsten.infura.io/v3/1b4540028fcd48cfa8d2167e17cf3db1"));
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "버전 : " + web3j.web3ClientVersion().send().getWeb3ClientVersion());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            //지갑 생성
           /* String filePath = WalletUtils.generateNewWalletFile("1234", new File(getFilesDir().toString()), false);
            Log.d(TAG, "" + filePath);*/

            //지갑 열기
            credentials = WalletUtils.loadCredentials("1234", getFilesDir() + "/UTC--2020-09-04T01-54-48.016--21eb0d125e181784d5e760dd66f1321476a0d9e9.json");
            Log.d(TAG, "주소 : " + credentials.getAddress());


            EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
            Log.d(TAG, ethGetBalance.getBalance().toString());

           /* Log.d(TAG, "이더 송금 시작");
            TransactionReceipt transactionReceipt = Transfer.sendFundsAsync(
                    web3j,
                    credentials,d
                    "0x69e504E9Ff46BB6BEfD78A91A856263023061D7C",
                    BigDecimal.valueOf(1.0),
                    Convert.Unit.GWEI
            ).get();*/


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //smart contrat....
        //contract addr = 0x1ae7954BC0f3CfAAb7bd4562C71a6C17d7cDBd4c


        Log.d(TAG, retrieve());
        Log.d(TAG, store(123));


    }

    public String retrieve() {
        Log.d(TAG, "retrieve()...");
        Function function = new Function(
                "retrieve",
                Collections.emptyList(),
                Arrays.asList(new TypeReference[]{})
        );

        return ethCall(function);
    }

    private BigInteger getNonce() throws ExecutionException, InterruptedException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.LATEST
        ).sendAsync().get();
        return ethGetTransactionCount.getTransactionCount();
    }
    public String ethCall(Function function) {
        Log.d(TAG, "ethCall().."+credentials.getAddress());
        String encodedFunction = FunctionEncoder.encode(function);
        //Nonce
        try {

            BigInteger nonce = getNonce();
            Log.d(TAG, "nonce : "+nonce);
            Transaction transaction = Transaction.createEthCallTransaction(credentials.getAddress(), "0x93Ff224E72255390e1D83729c7a48B4cce04CEbD", encodedFunction);

            EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List decode = FunctionReturnDecoder.decode(ethCall.getResult(), function.getOutputParameters());
            Log.d(TAG, ethCall.getResult()+"");



                    EthSendTransaction transactionResponse = web3j.ethSendTransaction(transaction).sendAsync().get();
                    String transactionHash = transactionResponse.getTransactionHash();
                    Log.d(TAG, "TransactionHash : " + transactionHash);


        } catch (ExecutionException e) {
            e.printStackTrace();
            return "fail";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "fail";
        }



        return "success";
    }
    private String store(int num){
        Function function = new Function(
                "store",
                Arrays.asList(new Uint256(123)),
                Collections.emptyList()
        );

        return storeEthCall(function, num);
    }
    private String storeEthCall(Function function, int num){
        Log.d(TAG, "storeEthCall()...");

        String encodedFunction = FunctionEncoder.encode(function);

        try {
            admin.personalUnlockAccount(credentials.getAddress(), "1234").sendAsync().get();

            BigInteger nonce = getNonce();

            Transaction transaction = Transaction.createFunctionCallTransaction(credentials.getAddress(), nonce  ,Transaction.DEFAULT_GAS,BigInteger.valueOf(40000),"0x93Ff224E72255390e1D83729c7a48B4cce04CEbD",   encodedFunction );

            //SendRawTransaction

            web3j.ethSendRawTransaction(transaction);

            /*EthSendTransaction...
            EthSendTransaction transactionResponse = web3j.ethSendTransaction(transaction).sendAsync().get();
            Log.d(TAG,"Error : " + transactionResponse.getError().getMessage());
            System.out.println(transactionResponse.getResult());
            Log.d(TAG, "Hash : "+transactionResponse.getTransactionHash());*/
        } catch (ExecutionException e) {
            e.printStackTrace();
            return "fail";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "fail";
        }




        return "success";
    }

}