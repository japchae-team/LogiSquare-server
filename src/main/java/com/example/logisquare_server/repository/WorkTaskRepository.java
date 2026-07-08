package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.task.WorkTask;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkTaskRepository extends JpaRepository<WorkTask, Long> {

    long countByStatusIn(Collection<String> statuses);

    long countByTaskTypeAndStatusIn(String taskType, Collection<String> statuses);

    @Query("""
            select count(task)
            from WorkTask task
            where task.taskType = :taskType
              and task.status in :statuses
              and not exists (
                  select assignment.id
                  from TaskAssignment assignment
                  where assignment.task = task
                    and assignment.status = :acceptedStatus
              )
            """)
    long countPendingTasksWithoutAcceptedAssignment(
            @Param("taskType") String taskType,
            @Param("statuses") Collection<String> statuses,
            @Param("acceptedStatus") String acceptedStatus
    );

    List<WorkTask> findTop5ByStatusInOrderByCreatedAtDesc(Collection<String> statuses);

    List<WorkTask> findTop5ByTaskTypeAndStatusInOrderByCreatedAtAsc(String taskType, Collection<String> statuses);

    List<WorkTask> findAllByTaskTypeAndStatusInOrderByCreatedAtAsc(String taskType, Collection<String> statuses);

    @Query("""
            select task
            from WorkTask task
            where task.taskType = :taskType
              and task.status in :statuses
              and not exists (
                  select assignment.id
                  from TaskAssignment assignment
                  where assignment.task = task
                    and assignment.status = :acceptedStatus
              )
            order by task.createdAt asc
            """)
    List<WorkTask> findPendingTasksWithoutAcceptedAssignmentOrderByCreatedAtAsc(
            @Param("taskType") String taskType,
            @Param("statuses") Collection<String> statuses,
            @Param("acceptedStatus") String acceptedStatus
    );

    List<WorkTask> findAllByStatus(String status);

    List<WorkTask> findAllByRequestedById(Long requestedById);
}
