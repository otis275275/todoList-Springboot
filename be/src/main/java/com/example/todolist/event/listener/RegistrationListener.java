package com.example.todolist.event.listener;

import com.example.todolist.event.OnRegistrationCompleteEvent;
import com.example.todolist.model.User;
import com.example.todolist.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
//Listerner để lắng nghe sự kiện đăng ký hoàn tất, sẽ được gọi khi có sự kiện OnRegistrationCompleteEvent xảy ra
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    @Autowired
    private IUserService service;

    @Autowired
    private JavaMailSender mailSender;

    //Hàm bắt buộc phải cài đặt khi implement ApplicationListener, 
    // sẽ được gọi khi có sự kiện OnRegistrationCompleteEvent xảy ra
    //Tự động gọi hàm này khi có sự kiện đăng ký hoàn tất
    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event); //Bóc tách sự kiện để xử lý hàm bên dưới
    }

    //Hàm logic chính để xử lý khi có sự kiện đăng ký hoàn tất, sẽ gửi email xác nhận đến người dùng
    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString(); //Tạo token ngẫu nhiên
        service.createVerificationToken(user, token);

        // 2. SOẠN EMAIL
        String recipientAddress = user.getEmail(); // Lấy email người nhận
        String subject = "Registration Confirmation"; // Tiêu đề email
        
        // Đính cái chuỗi token vừa tạo vào đường link xác nhận
        String confirmationUrl = event.getAppUrl() + "/api/auth/registrationConfirm?token=" + token;
        
        String message = "You registered successfully. To confirm your registration, please click on the below link.";

        // 3. GỬI EMAIL
        SimpleMailMessage email = new SimpleMailMessage(); // Tạo đối tượng thư đơn giản
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + "\r\n" + confirmationUrl); // Nội dung thư kèm link bấm xác nhận
        
        mailSender.send(email); // Thực hiện lệnh gửi mail qua mạng mạng!
    }
}