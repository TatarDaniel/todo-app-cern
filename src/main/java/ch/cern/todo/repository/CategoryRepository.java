package ch.cern.todo.repository;

import ch.cern.todo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category>, PagingAndSortingRepository<Category, Long> {
    Optional<Category> findByName(String name);

    //Optional<Category> findCategoryById(Long id);
}
