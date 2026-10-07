package com.manideep.work_tracker.Repo;

import com.manideep.work_tracker.entity.WorkItem;
import com.manideep.work_tracker.entity.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {

    List<WorkItem> findByStatusOrderByCreatedAtDesc(
            WorkStatus status
    );

    List<WorkItem> findAllByOrderByCreatedAtDesc();

    List<WorkItem> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );

    List<WorkItem> findByCompletedAtBetweenOrderByCompletedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );
}