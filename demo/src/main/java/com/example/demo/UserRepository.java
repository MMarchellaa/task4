package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<ArrayList<User>> getUsersByLastTimeClickedBroButtonIsNotNull();

    Optional<ArrayList<User>> getUsersByLastTimeClickedSisButtonIsNotNull();

    Optional<User> findUserByName(String name);
}
