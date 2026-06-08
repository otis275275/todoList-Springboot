package com.example.todolist.service;

import com.example.todolist.model.User;

public interface IUserService {
    User findByUsername(String username);
    User findByEmail(String email);
    User save(User user);
    void createVerificationToken(User user, String token);
    String validateVerificationToken(String token);
}