package hello.partitioner;

import com.db.json.Portfolio;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08.08.16.
 */
public class ServiceCallsPartitioner implements Partitioner {

    private List<Portfolio> portfolios;

    public ServiceCallsPartitioner(List<Portfolio> portfolios){
        this.portfolios = portfolios;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        if(portfolios.size() != gridSize){
            throw new IllegalStateException("Number of Portfolios does not match number of sub-steps");
        }
        Map<String, ExecutionContext> executionContextMap = new HashMap<>(portfolios.size());
        for(Portfolio portfolio : portfolios){
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.put("portfolio", portfolio);
            executionContextMap.put(portfolio.getFakeId().toString(), executionContext);
        }
        return executionContextMap;
    }
}
