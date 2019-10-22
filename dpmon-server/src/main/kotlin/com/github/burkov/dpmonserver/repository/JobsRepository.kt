package com.github.burkov.dpmonserver.repository

import com.github.burkov.dpmonserver.models.Job
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobsRepository : JpaRepository<Job, Int> {

}