package com.microservices.jobms.job;

import com.microservices.jobms.job.dto.JobDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

   private final JobService jobService;
   public JobController(JobService jobService){
       this.jobService = jobService;
   }

    @GetMapping
    public ResponseEntity<List<JobDTO>> findAll(){
        return ResponseEntity.ok(jobService.findAll());
    }

    @PostMapping
    public ResponseEntity<String> createJob(@RequestBody Job job){
        boolean created = jobService.createJob(job);
        if (created) {
            return new ResponseEntity<>("Job added successfully", HttpStatus.CREATED);
        }
        return new ResponseEntity<>("Company not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDTO> getJobById(@PathVariable Long id){
        JobDTO job = jobService.findById(id);
        if(job != null){
            return ResponseEntity.ok(job);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<String> updateJob(@RequestBody Job updatedJjob, @PathVariable Long id){
         boolean updated = jobService.updateJobById(id, updatedJjob);
         if(updated){
             return ResponseEntity.ok("Job updated successfully");
         }else {
             return ResponseEntity.notFound().build();
         }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJobById(@PathVariable Long id){
       boolean deleted = jobService.deleteJobById(id);
       if(deleted) {
           return ResponseEntity.ok("Job deleted successfully");
       }else {
           return ResponseEntity.notFound().build();
       }
    }
}
