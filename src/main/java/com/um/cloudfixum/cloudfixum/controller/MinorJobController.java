package com.um.cloudfixum.cloudfixum.controller;

import com.um.cloudfixum.cloudfixum.model.Category;
import com.um.cloudfixum.cloudfixum.model.MinorJob;
import com.um.cloudfixum.cloudfixum.model.ProviderUser;
import com.um.cloudfixum.cloudfixum.service.MinorJobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@CrossOrigin
@RequestMapping("/api/service")
public class MinorJobController {

    private final MinorJobService minorJobService;

    public MinorJobController(MinorJobService minorJobService) {
        this.minorJobService = minorJobService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<MinorJob>> getAllService(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, HttpServletRequest request) {

        size = (size == null || size < 1) ? 9 : size;
        return (page == null || page < 0) ? minorJobService.getAll() : minorJobService.findByPage(page, size, request, minorJobService.getRepository().findAll());
    }

    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<MinorJob>> filterService(@RequestParam(value = "text", required = false) String text, @RequestParam(value = "subquery", required = false) Category sub_query, @RequestParam(value = "superquery", required = false) String super_query, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, HttpServletRequest request){
        List<MinorJob> minorJobList = minorJobService.getRepository().findAll();
        minorJobList = (text != null) ? minorJobService.filterByTitleOrDescription(text,text) : minorJobList;
        minorJobList = (sub_query != null) ? minorJobList.stream().filter(e -> e.getCategory().equals(sub_query)).collect(Collectors.toList()) : minorJobList;
        minorJobList = (super_query != null) ? minorJobList.stream().filter(e -> e.getCategory().getSuperCategory().equalsIgnoreCase(super_query)).collect(Collectors.toList()) : minorJobList;
        if (minorJobList.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        size = (size == null || size < 1) ? 9 : size;
        return (page == null || page < 0) ? new ResponseEntity<>(minorJobList,HttpStatus.OK) : minorJobService.findByPage(page, size, request,minorJobList);
    }
    @GetMapping("/{id}")
    public ResponseEntity<MinorJob> getServiceByID(@PathVariable Long id) {
        return minorJobService.getById(id);
    }


    @PostMapping
    public ResponseEntity<MinorJob> addService(@Valid @RequestBody MinorJob service, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated())
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        ProviderUser serviceProvider = minorJobService.getServiceProviderByToken(authentication);

        service.setServiceProvider(serviceProvider);
        service.setDate(LocalDate.now());
        service.setImage_url(service.getCategory().getImage_url());

        return minorJobService.create(service);

    }

    @PutMapping
    public ResponseEntity<MinorJob> updateService(@Valid @RequestBody MinorJob minorJob, Authentication authentication) {

        if (!minorJobService.isOwner(authentication, minorJob)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        minorJob.setImage_url(minorJob.getCategory().getImage_url());
        return minorJobService.update(minorJob);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteService(@PathVariable Long id, Authentication authentication) {
        Optional<MinorJob> minorJob = minorJobService.getRepository().findById(id);

        if (!minorJob.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return minorJobService.isOwner(authentication, minorJob.get()) ? minorJobService.delete(id) : new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/{id}/average")
    public ResponseEntity<Map<String,Float>> getAverageBudgetsByMinorJob(@PathVariable Long id) {
        return minorJobService.getMinorJobAverage(id);
    }
}
