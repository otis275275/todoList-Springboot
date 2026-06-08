package com.example.todolist.controller;

import com.example.todolist.model.Todo;
import com.example.todolist.model.User;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.repository.UserRepository;
import com.example.todolist.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<Todo> getTodos() {
        return todoRepository.findByUserId(getCurrentUser().getId());
    }

    @PostMapping
    public Todo createTodo(@RequestBody Todo todo) {
        todo.setUser(getCurrentUser());
        return todoRepository.save(todo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, @RequestBody Todo todoDetails) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));

        if (!todo.getUser().getId().equals(getCurrentUser().getId())) {
            return ResponseEntity.status(403).build();
        }

        todo.setTitle(todoDetails.getTitle());
        todo.setCompleted(todoDetails.isCompleted());
        final Todo updatedTodo = todoRepository.save(todo);
        return ResponseEntity.ok(updatedTodo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        //Đảm bảo chỉ có chủ sở hữu của todo mới có thể xóa nó
        //So sánh user của todo được yêu cầu API với user hiện tại ở context, nếu không khớp thì trả về lỗi 403 Forbidden
        if (!todo.getUser().getId().equals(getCurrentUser().getId())) {
            return ResponseEntity.status(403).build();
        }

        todoRepository.delete(todo);
        return ResponseEntity.ok().build();
    }
}
