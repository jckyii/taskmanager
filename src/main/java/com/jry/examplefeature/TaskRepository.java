package com.jry.examplefeature;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

}
