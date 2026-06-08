import React, { useState } from 'react';
import MfaService from '../services/mfa.service';

/**
 * Component bước 2 của luồng đăng nhập khi MFA đã bật.
 *
 * Kịch bản:
 * 1. User đăng nhập đúng username/password
 * 2. BE trả về { requiresMfa: true, tempToken }
 * 3. Login.js phát hiện requiresMfa=true → render <MfaOtp />
 * 4. User mở Google Authenticator, nhập mã 6 số vào đây
 * 5. Gọi POST /api/auth/mfa/verify với tempToken + code
 * 6. Thành công → BE trả JWT thật → onVerifySuccess() → vào dashboard
 *
 * Props:
 * - tempToken: JWT tạm nhận từ bước login
 * - onVerifySuccess: callback gọi khi verify thành công
 * - onBack: callback quay lại màn login
 */
const MfaOtp = ({ tempToken, onVerifySuccess, onBack }) => {
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleVerify = async (e) => {
    e.preventDefault();
    setMessage('');
    setLoading(true);

    try {
      // Gọi /api/auth/mfa/verify với tempToken + mã OTP
      await MfaService.verifyMfa(tempToken, code);
      // Thành công → JWT thật đã được lưu vào localStorage bởi mfa.service.js
      onVerifySuccess();
    } catch (error) {
      const msg =
        error.response?.data?.message ||
        'Mã OTP không đúng hoặc đã hết hạn.';
      setMessage(msg);
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto bg-white dark:bg-gray-800 rounded-lg shadow-md p-8">
      {/* Icon shield */}
      <div className="flex justify-center mb-4">
        <div className="w-16 h-16 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
          <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
            />
          </svg>
        </div>
      </div>

      <h2 className="text-2xl font-bold text-center mb-2">Xác thực 2 bước</h2>
      <p className="text-center text-gray-500 dark:text-gray-400 mb-6 text-sm">
        Mở <strong>Google Authenticator</strong>, nhập mã 6 số hiển thị cho tài khoản này.
      </p>

      <form onSubmit={handleVerify}>
        <div className="mb-6">
          <label className="block text-sm font-medium mb-2" htmlFor="otp-code">
            Mã OTP (6 số)
          </label>
          <input
            type="text"
            id="otp-code"
            className="w-full px-4 py-3 border rounded-lg text-center text-2xl tracking-[0.5em] font-mono dark:bg-gray-700 dark:border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="000000"
            value={code}
            onChange={(e) => {
              // Chỉ cho nhập số, tối đa 6 ký tự
              const val = e.target.value.replace(/\D/g, '').slice(0, 6);
              setCode(val);
            }}
            maxLength={6}
            inputMode="numeric"
            autoComplete="one-time-code"
            autoFocus
            required
          />
        </div>

        <button
          type="submit"
          className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
          disabled={loading || code.length !== 6}
        >
          {loading ? 'Đang xác thực...' : 'Xác nhận'}
        </button>

        {message && (
          <p className="mt-4 text-center text-red-500 text-sm">{message}</p>
        )}
      </form>

      {/* Nút quay lại */}
      <button
        onClick={onBack}
        className="mt-4 w-full text-center text-sm text-gray-500 hover:text-gray-700 dark:hover:text-gray-300 transition-colors"
      >
        ← Quay lại đăng nhập
      </button>
    </div>
  );
};

export default MfaOtp;
