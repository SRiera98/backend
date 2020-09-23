package com.um.cloudfixum.cloudfixum.controller;

import com.um.cloudfixum.cloudfixum.model.MinorJob;
import com.um.cloudfixum.cloudfixum.service.MinorJobService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;


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
    public ResponseEntity<List<MinorJob>> getAllService(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "q", required = false) String q, HttpServletRequest request) {
        //System.out.println(minorJobService.filterBySubCategory(q));
        ResponseEntity<List<MinorJob>> jobList = (q != null) ? minorJobService.filterByTitle(q) : minorJobService.getAll();
        size = (size == null || size < 1) ? 9 : size;
        return (page == null || page < 0) ? jobList : minorJobService.findByPage(page, size, request); //antes despues del ? estaba el getAll()
    }

    @GetMapping("/{id}")
    public ResponseEntity<MinorJob> getServiceByID(@PathVariable Long id) {
        return minorJobService.getById(id);
    }

    @PostMapping
    public ResponseEntity<MinorJob> addService(@Valid @RequestBody MinorJob service) {
        service.setDate(LocalDate.now());
        service.setImage_url(service.getCategory().getImage_url());

        return minorJobService.create(service);
    }

    @PutMapping
    public ResponseEntity<MinorJob> updateService(@Valid @RequestBody MinorJob minorJob) {
        minorJob.setImage_url(minorJob.getCategory().getImage_url());

        return minorJobService.update(minorJob);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteService(@PathVariable Long id) {
        return minorJobService.delete(id);
    }
}
