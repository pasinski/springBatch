package hello.service;

import com.db.json.Portfolio;
import com.db.json.Transaction;
import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by michal on 08.08.16.
 */
@Component
public class FakeTransactionsService {

    private Faker faker = new Faker();
    private Random random = new Random();

    public List<Transaction> getTransactionsForPortfolio(Portfolio portfolio) {
        return createFakeTransactionsForPortfolio(portfolio);
    }

    private List<Transaction> createFakeTransactionsForPortfolio(Portfolio portfolio) {
        int randomSize = random.nextInt(50);
        List<Transaction> transactions = new ArrayList<>(randomSize);
        for(int i = 0; i < randomSize; i++){
            transactions.add(createFakeTransaction(portfolio));
        }
        return transactions;
    }

    private Transaction createFakeTransaction(Portfolio portfolio) {
        Transaction transaction = new Transaction();
        transaction.setDescription("PortfolioID is " + portfolio.getFakeId());
        transaction.setCredit(faker.number().randomDouble(2, 10, 100));
        transaction.setDebit(faker.number().randomDouble(2, 10, 100));

        return transaction;
    }

}
