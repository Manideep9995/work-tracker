package com.manideep.work_tracker.service;

import com.manideep.work_tracker.entity.WorkItem;
import com.manideep.work_tracker.entity.WorkStatus;
import com.manideep.work_tracker.Repo.WorkItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkItemService {

    private final WorkItemRepository workItemRepository;

    public WorkItemService(WorkItemRepository workItemRepository) {
        this.workItemRepository = workItemRepository;
    }

    public List<WorkItem> getAllWorks() {
        return workItemRepository.findAllByOrderByCreatedAtDesc();
    }

    public WorkItem addWork(String title, String description) {

        WorkItem workItem = new WorkItem();

        workItem.setTitle(title);
        workItem.setDescription(description);

        return workItemRepository.save(workItem);
    }

    public void completeWork(Long id) {

        WorkItem workItem = workItemRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Work item not found: " + id
                        )
                );

        if (workItem.getStatus() == WorkStatus.COMPLETED) {
            return;
        }

        workItem.setStatus(WorkStatus.COMPLETED);
        workItem.setCompletedAt(LocalDateTime.now());

        workItemRepository.save(workItem);
    }

    public void deleteWork(Long id) {

        if (!workItemRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Work item not found: " + id
            );
        }

        workItemRepository.deleteById(id);
    }
    public List<WorkItem> getPendingWorks() {

        return workItemRepository
                .findByStatusOrderByCreatedAtDesc(
                        WorkStatus.PENDING
                );
    }
    public List<WorkItem> getCompletedWorks() {

        return workItemRepository
                .findByStatusOrderByCreatedAtDesc(
                        WorkStatus.COMPLETED
                );
    }
}
