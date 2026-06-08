import axios from 'axios';

const API_URL = 'http://localhost:8081/api/auth/';

class AuthService {
  /**
   * Đăng nhập với username + password.
   *
   * Có 2 trường hợp response từ BE:
   * [1] MFA chưa bật: { token, id, username, role, requiresMfa: false }
   *     → Lưu vào localStorage ngay → vào dashboard
   *
   * [2] MFA đã bật: { requiresMfa: true, tempToken, id, username, role }
   *     → KHÔNG lưu vào localStorage
   *     → Trả về response để Login component chuyển sang màn nhập OTP
   *
   * @returns response.data (component tự kiểm tra requiresMfa)
   */
  async login(username, password) {
    const response = await axios.post(API_URL + 'signin', { username, password });

    if (response.data.token) {
      // Trường hợp 1: login thành công hoàn toàn, lưu JWT thật
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    // Trường hợp 2: requiresMfa=true → component sẽ tự xử lý, không lưu gì ở đây

    return response.data;
  }

  logout() {
    localStorage.removeItem('user');
  }

  async register(username, email, password) {
    const response = await axios.post(API_URL + 'signup', { username, email, password });
    return response.data;
  }

  getCurrentUser() {
    return JSON.parse(localStorage.getItem('user'));
  }
}

export default new AuthService();
