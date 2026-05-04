package com.ordering.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ordering.system.entity.Labor;

@Repository
public interface LaborRepository extends JpaRepository<Labor, Long> {

    /**
     * Finds the labor profile associated with a specific system account ID.
     * This is critical for breaking the database link before revoking a user.
     */
    Labor findBySystemAccountId(Long userId);

    /**
     * Directly deletes the labor profile linked to a system account.
     * Use this in your UserController delete logic to prevent 500 errors.
     */
    void deleteBySystemAccountId(Long userId);
}