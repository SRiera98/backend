package com.um.cloudfixum.cloudfixum.repository;
import com.um.cloudfixum.cloudfixum.model.MinorJob;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MinorJobRepository extends JpaRepository <MinorJob, Long> {
    List<MinorJob> findByTitleContainingOrDescriptionContainingIgnoreCase(String title_query, String description_query);
}
