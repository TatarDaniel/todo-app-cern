package ch.cern.todo.repository;

import ch.cern.todo.entity.Category;
import ch.cern.todo.entity.Task;
import ch.cern.todo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task>, PagingAndSortingRepository<Task, Long> {
    List<Task> findByCategoryId(Long categoryId);

    Optional<Task> findByNameAndCategoryAndCreatedBy(String name, Category category, User user);
}
