package com.microservices.jobms.job;


import com.microservices.jobms.job.clients.CompanyClient;
import com.microservices.jobms.job.clients.ReviewClient;
import com.microservices.jobms.job.dto.JobDTO;
import com.microservices.jobms.job.external.Company;
import com.microservices.jobms.job.external.Review;
import com.microservices.jobms.job.mapper.JobMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    //private List<Job> jobs= new ArrayList<>();

//    RestTemplate restTemplate;
    private final JobRepository jobRepository;
    private final CompanyClient companyClient;
    private final ReviewClient reviewClient;

    int attempt = 0;

    public JobServiceImpl(JobRepository jobRepository, CompanyClient companyClient, ReviewClient reviewClient) {
        this.jobRepository = jobRepository;
        this.companyClient = companyClient;
        this.reviewClient = reviewClient;
    }

    @Override
//  @CircuitBreaker(name = "companyBreaker", fallbackMethod = "companyBreakerFallback")
//  @Retry(name = "companyBreaker", fallbackMethod = "companyBreakerFallback")
    @RateLimiter(name = "companyBreaker", fallbackMethod = "companyBreakerFallback")
    @Transactional(readOnly = true)
    public List<JobDTO> findAll() {
        System.out.println("Attempt: " + (++attempt));
        List<Job> jobs = jobRepository.findAll();
        return jobs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<String> companyBreakerFallback(Exception e) {
        List<String> jobs = new ArrayList<>();
        jobs.add("Dummy");
        return jobs;
    }

    @Override
    public JobDTO findById(Long id) {
        Optional<Job> jobOptional = jobRepository.findById(id);
        if(jobOptional.isPresent()) {
            Job job = jobOptional.get();
            return convertToDTO(job);
        }
        return null;
    }

    private JobDTO convertToDTO(Job job){

        Company company = companyClient.getCompany(job.getCompanyId());
        List<Review> reviews = reviewClient.getReviews(job.getCompanyId());
//        ResponseEntity<List<Review>> reviewResponse =
//                restTemplate.exchange("http://REVIEW-SERVICE:8083/reviews?companyId=" + job.getCompanyId(),
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<Review>>() {});

//        List<Review> reviews = reviewResponse.getBody();
        return JobMapper.mapToJobWithCompanyDTO(job, company, reviews);
    }

    @Override
    public boolean createJob(Job job) {
        try{
            jobRepository.save(job);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean deleteJobById(Long id) {
        try{
            jobRepository.deleteById(id);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean updateJobById(Long id, Job updatedJob) {
        Optional<Job> jobOptional = jobRepository.findById(id);
        if(jobOptional.isPresent()) {
            Job job = jobOptional.get();
            if (job.getId().equals(id)) {
                job.setTitle(updatedJob.getTitle());
                job.setDescription(updatedJob.getDescription());
                job.setMaxSalary(updatedJob.getMaxSalary());
                job.setMinSalary(updatedJob.getMinSalary());
                job.setMaxSalary(updatedJob.getMaxSalary());
                job.setLocation(updatedJob.getLocation());
                jobRepository.save(job);
                return true;
            }
        }
        return false;
    }
}
