import axios from 'axios';
import authHeader from './auth-header';

// Base URL của các MFA endpoint
const MFA_URL = 'http://localhost:8081/api/auth/mfa/';

/**
 * Service xử lý các tác vụ MFA từ phía Frontend.
 *
 * Gồm 4 chức năng tương ứng với 4 endpoint BE:
 * - setupMfa()    → GET  /setup    — Lấy QR code (cần JWT thật)
 * - activateMfa() → POST /activate — Xác nhận lần đầu quét QR thành công (cần JWT thật)
 * - verifyMfa()   → POST /verify   — Xác thực OTP khi login (dùng tempToken)
 * - disableMfa()  → POST /disable  — Tắt MFA (cần JWT thật)
 */
const MfaService = {

  /**
   * Lấy QR code để user quét vào Google Authenticator.
   * Yêu cầu: đã đăng nhập (có JWT thật trong localStorage).
   * Response: { qrCode: "data:image/png;base64,...", secret: "BASE32..." }
   */
  async setupMfa() {
    const response = await axios.get(MFA_URL + 'setup', {
      headers: authHeader(), // Gửi kèm JWT thật trong header Authorization
    });
    return response.data;
  },

  /**
   * Xác nhận mã OTP lần đầu để chính thức bật MFA.
   * Gọi sau khi user quét QR và nhập thử mã 6 số.
   * Yêu cầu: đã đăng nhập (có JWT thật).
   *
   * @param {string} code - Mã 6 số từ Google Authenticator
   */
  async activateMfa(code) {
    const response = await axios.post(
      MFA_URL + 'activate',
      { code },
      { headers: authHeader() } // JWT thật
    );
    return response.data;
  },

  /**
   * Xác thực OTP trong luồng đăng nhập (bước 2 sau /signin).
   * Endpoint public — dùng tempToken từ response /signin thay vì JWT thật.
   *
   * @param {string} tempToken - JWT tạm thời nhận được từ bước login
   * @param {string} code      - Mã 6 số từ Google Authenticator
   * @returns JWT thật nếu thành công
   */
  async verifyMfa(tempToken, code) {
    const response = await axios.post(MFA_URL + 'verify', {
      tempToken,
      code,
    });
    // Nếu verify thành công, lưu JWT thật vào localStorage (giống flow login bình thường)
    if (response.data.token) {
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  /**
   * Tắt MFA — user phải xác nhận bằng OTP hiện tại trước.
   * Yêu cầu: đã đăng nhập (có JWT thật).
   *
   * @param {string} code - Mã 6 số từ Google Authenticator
   */
  async disableMfa(code) {
    const response = await axios.post(
      MFA_URL + 'disable',
      { code },
      { headers: authHeader() } // JWT thật
    );
    return response.data;
  },
};

export default MfaService;
