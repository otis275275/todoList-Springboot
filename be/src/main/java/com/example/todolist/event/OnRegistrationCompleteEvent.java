package com.example.todolist.event;

import com.example.todolist.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private String appUrl;
    private User user;

    public OnRegistrationCompleteEvent(User user, String appUrl) {
        super(user); //Gọi constructor của lớp cha ApplicationEvent với đối tượng user làm nguồn sự kiện
        this.user = user;
        this.appUrl = appUrl;
    }
}