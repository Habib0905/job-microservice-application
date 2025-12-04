package com.project.jobms.job.impl;

import com.project.jobms.job.Job;
import com.project.jobms.job.JobRepository;
import com.project.jobms.job.JobService;
import com.project.jobms.job.clients.CompanyClient;
import com.project.jobms.job.clients.ReviewClient;
import com.project.jobms.job.dto.JobDTO;
import com.project.jobms.job.external.Company;
import com.project.jobms.job.external.Review;
import com.project.jobms.job.mapper.JobMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    private JobRepository jobRepository;
    private CompanyClient companyClient;
    private ReviewClient reviewClient;

    @Autowired
    RestTemplate restTemplate;



    public JobServiceImpl(JobRepository jobRepository, CompanyClient companyClient, ReviewClient reviewClient) {
        this.jobRepository = jobRepository;
        this.companyClient = companyClient;
        this.reviewClient = reviewClient;
    }


    @Override
    public List<JobDTO> findAll() {

        List<Job> jobs = jobRepository.findAll();

        return jobs.stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private JobDTO convertToDTO(Job job) {
//        RestTemplate restTemplate = new RestTemplate();

        JobMapper jobMapper = new JobMapper();
        Company company = companyClient.getCompany(job.getCompanyId());
        List<Review> reviews = reviewClient.getReviews(job.getCompanyId());

        return jobMapper.mapToJobWithCompanyDTO(job, company, reviews);

    }

    @Override
    public void createJob(Job job) {
        jobRepository.save(job);
    }

    @Override
    public JobDTO findJobById(Long id) {
        Job job =  jobRepository.findById(id).orElse(null);
        return convertToDTO(job);
    }

    @Override
    public boolean deleteJobById(Long id) {
        if (jobRepository.existsById(id)) {
            jobRepository.deleteById(id);
            return true;
        }
        return false;


    }

    @Override
    @Transactional
    public boolean updateJob(Long id, Job updatedJob) {
        Optional<Job> jobOptional = jobRepository.findById(id);
            if(jobOptional.isPresent()) {
                Job job = jobOptional.get();
                job.setTitle(updatedJob.getTitle());
                job.setDescription(updatedJob.getDescription());
                job.setLocation(updatedJob.getLocation());
                job.setMaxSalary(updatedJob.getMaxSalary());
                job.setMinSalary(updatedJob.getMinSalary());
                return true;
            }

        return false;
    }


}