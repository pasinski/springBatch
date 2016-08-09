package hello.processor;

import com.db.json.Portfolio;
import com.db.json.Transaction;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Created by michal on 08.08.16.
 */
public class PortfolioTransactionsProcessor implements ItemProcessor<List<Transaction>, Portfolio>, StepExecutionListener {
    private Portfolio portfolio;

    @Override
    public Portfolio process(List<Transaction> transactions) throws Exception {
        portfolio.setTransactions(transactions);
        return portfolio;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    @Override
    public void beforeStep(org.springframework.batch.core.StepExecution stepExecution) {
        ExecutionContext ctx = stepExecution.getExecutionContext();
        this.setPortfolio((Portfolio) ctx.get("portfolio"));
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
