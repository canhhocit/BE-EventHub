package com.sa.event_mng.modules.event.domain.repository;

import com.sa.event_mng.modules.event.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
