package com.microservices.jobms.job;

import com.microservices.jobms.job.dto.JobDTO;

import java.util.List;

public interface JobService {

    List<JobDTO> findAll();
    boolean createJob(Job job);
    JobDTO findById(Long id);
    boolean deleteJobById(Long id);
    boolean updateJobById(Long id, Job updatedJob);
}
