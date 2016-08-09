package hello.reader;

import com.db.json.Portfolio;
import com.db.json.Transaction;
import hello.service.FakeTransactionsService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Value;

import javax.batch.runtime.StepExecution;
import java.util.List;

/**
 * Created by michal on 08.08.16.
 */
public class PartitionedListReader implements ItemReader<List<Transaction>>, StepExecutionListener{

    private Portfolio portfolio;
    private FakeTransactionsService fakeTransactionsService;

    public PartitionedListReader(FakeTransactionsService fakeTransactionsService){
        this.fakeTransactionsService = fakeTransactionsService;
    }

    @Override
    public List<Transaction> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return fakeTransactionsService.getTransactionsForPortfolio(portfolio);
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
    public ExitStatus afterStep(org.springframework.batch.core.StepExecution stepExecution) {
        return null;
    }
}
