package com.um.cloudfixum.cloudfixum.service;

import com.um.cloudfixum.cloudfixum.common.GenericServiceImpl;
import com.um.cloudfixum.cloudfixum.model.Budget;
import com.um.cloudfixum.cloudfixum.model.BudgetStatus;
import com.um.cloudfixum.cloudfixum.model.MinorJob;
import com.um.cloudfixum.cloudfixum.model.ProviderUser;
import com.um.cloudfixum.cloudfixum.repository.BudgetRepository;
import com.um.cloudfixum.cloudfixum.repository.MinorJobRepository;
import com.um.cloudfixum.cloudfixum.repository.ProviderUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MinorJobService extends GenericServiceImpl<MinorJob> {

    private final MinorJobRepository minorJobRepository;
    private final ProviderUserRepository providerUserRepository;
    private final BudgetRepository budgetRepository;

    public MinorJobService(MinorJobRepository minorJobRepository, ProviderUserRepository providerUserRepository, BudgetRepository budgetRepository) {
        this.minorJobRepository = minorJobRepository;
        this.providerUserRepository = providerUserRepository;
        this.budgetRepository = budgetRepository;
    }

    public ProviderUser getServiceProviderByToken(Authentication jwt) {
        return providerUserRepository.findByEmail(jwt.getName());
    }

    public Boolean isOwner(Authentication auth, MinorJob minorJob) {
        if(auth == null || auth.getName() == null) {
            return Boolean.FALSE;
        }

        if(!minorJob.getServiceProvider().getId().equals(providerUserRepository.findByEmail(auth.getName()).getId())){
            return Boolean.FALSE;
        }

        return Boolean.TRUE;

    }

    @Override
    public ResponseEntity<MinorJob> create(MinorJob job) {

        Optional<ProviderUser> serviceProvider = providerUserRepository.findById(job.getServiceProvider().getId());
        if (!serviceProvider.isPresent()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if (serviceProvider.get().getServiceList().contains(job)) return new ResponseEntity<>(HttpStatus.CONFLICT);

        job.setServiceProvider(serviceProvider.get());
        return super.create(job);
    }

    public List<MinorJob> filterByTitleOrDescription(String query_title, String query_description) {
        return minorJobRepository.findByTitleContainingOrDescriptionContainingIgnoreCase(query_title,query_description);
    }

    @Override
    public ResponseEntity<MinorJob> update(MinorJob job) {
        Optional<ProviderUser> serviceProvider = providerUserRepository.findById(job.getServiceProvider().getId());
        if (!serviceProvider.isPresent()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        job.setServiceProvider(serviceProvider.get());

        return super.update(job);
    }

    @Override
    public JpaRepository<MinorJob, Long> getRepository() {
        return minorJobRepository;
    }

    public ResponseEntity <Map<String,Float>> getMinorJobAverage(Long id) {
        if (!minorJobRepository.findById(id).isPresent()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<Budget> budgetsUserJobs = getQualifyBudgetByMinorJob(id);
        Float count = declaresCount();
        if (budgetsUserJobs.size() == 0){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        for (Budget budget : budgetsUserJobs){
            count += budget.getQualification();
        }

        Float average = count/budgetsUserJobs.size();
        Map<String, Float> jobAvg = new HashMap<>();
        jobAvg.put("average",average);

        return average == 0 ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(jobAvg,HttpStatus.OK);
    }

    private Float declaresCount(){
        return 0.0f;
    }

    private List<Budget> getQualifyBudgetByMinorJob(Long id) {

        return budgetRepository.findAllByMinorJobId(id)
                .stream()
                .filter(b -> b.getBudgetStatus().equals(BudgetStatus.BUDGET_ACCEPTED))
                .filter(b -> b.getQualification() != null)
                .collect(Collectors.toList());
    }
}
