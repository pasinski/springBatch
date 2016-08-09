package hello.service;

import com.db.json.Portfolio;
import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michal on 08.08.16.
 */
@Component
public class FakePortfoliosService {

    private Faker faker = new Faker();

    public List<Portfolio> getPortfolios(){
        List<Portfolio> portfolios = new ArrayList<>(100);
        for(int i = 0; i < 100; i++){
            portfolios.add(createFakePortfolio());
        }
        return portfolios;
    }

    private Portfolio createFakePortfolio() {
        Portfolio portfolio = new Portfolio();
        portfolio.setFakeId(faker.number().randomDouble(0, 0, 1000000));
        portfolio.setPortfolioNumber(faker.idNumber().ssnValid());
        portfolio.setName(faker.company().name());
        return portfolio;
    }
}
