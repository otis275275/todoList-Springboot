import React, { useState, useEffect } from 'react';
import MfaService from '../services/mfa.service';

/**
 * Component cho phép user bật/tắt MFA trong trang cài đặt.
 *
 * Có 2 chế độ tương ứng với trạng thái MFA:
 *
 * [A] MFA chưa bật (mfaEnabled=false):
 *     1. Hiện nút "Bật MFA"
 *     2. Click → gọi GET /setup → nhận QR code + secret
 *     3. Hiện ảnh QR để user quét vào Google Authenticator
 *     4. User nhập mã 6 số → gọi POST /activate
 *     5. Thành công → mfaEnabled = true
 *
 * [B] MFA đã bật (mfaEnabled=true):
 *     1. Hiện nút "Tắt MFA"
 *     2. User nhập mã OTP xác nhận
 *     3. Gọi POST /disable → mfaEnabled = false, secret bị xóa
 *
 * Props:
 * - isMfaEnabled: boolean — trạng thái MFA hiện tại của user
 * - onStatusChange: callback(newStatus) — báo App.js khi trạng thái MFA thay đổi
 */
const MfaSetup = ({ isMfaEnabled, onStatusChange }) => {
  // ===== State =====
  const [step, setStep] = useState('idle'); // 'idle' | 'setup' | 'activate' | 'disable'
  const [qrCode, setQrCode] = useState('');   // Data URI ảnh QR
  const [secret, setSecret] = useState('');   // Secret key dạng Base32 (để user nhập tay nếu cần)
  const [code, setCode] = useState('');        // Mã OTP 6 số user nhập
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ text: '', type: '' }); // type: 'success' | 'error'

  // Reset state khi mfaEnabled thay đổi từ ngoài
  useEffect(() => {
    setStep('idle');
    setCode('');
    setMessage({ text: '', type: '' });
  }, [isMfaEnabled]);

  // ========== Bước 1: Lấy QR code từ BE ==========
  const handleStartSetup = async () => {
    setLoading(true);
    setMessage({ text: '', type: '' });
    try {
      // Gọi GET /api/auth/mfa/setup — cần JWT thật trong header
      const data = await MfaService.setupMfa();
      setQrCode(data.qrCode);   // Data URI → dùng làm <img src>
      setSecret(data.secret);   // Base32 secret → cho user nhập tay
      setStep('setup');          // Chuyển sang màn hiển thị QR
    } catch (error) {
      setMessage({ text: 'Lỗi khi tải QR code. Vui lòng thử lại.', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  // ========== Bước 2: Kích hoạt MFA sau khi quét QR ==========
  const handleActivate = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ text: '', type: '' });
    try {
      // Gọi POST /api/auth/mfa/activate với mã 6 số user vừa nhập
      await MfaService.activateMfa(code);
      setMessage({ text: 'MFA đã được kích hoạt thành công! ✓', type: 'success' });
      setStep('idle');
      setCode('');
      onStatusChange(true); // Báo component cha biết MFA đã bật
    } catch (error) {
      const msg = error.response?.data?.message || 'Mã OTP không đúng. Vui lòng thử lại.';
      setMessage({ text: msg, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  // ========== Tắt MFA ==========
  const handleDisable = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ text: '', type: '' });
    try {
      // Gọi POST /api/auth/mfa/disable — phải xác nhận bằng OTP hiện tại
      await MfaService.disableMfa(code);
      setMessage({ text: 'MFA đã được tắt.', type: 'success' });
      setStep('idle');
      setCode('');
      onStatusChange(false); // Báo component cha biết MFA đã tắt
    } catch (error) {
      const msg = error.response?.data?.message || 'Mã OTP không đúng. Không thể tắt MFA.';
      setMessage({ text: msg, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  // ===== Render =====
  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h3 className="text-lg font-semibold">Xác thực 2 bước (MFA)</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Bảo vệ tài khoản bằng Google Authenticator
          </p>
        </div>
        {/* Badge trạng thái */}
        <span className={`px-3 py-1 rounded-full text-sm font-medium ${
          isMfaEnabled
            ? 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300'
            : 'bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400'
        }`}>
          {isMfaEnabled ? '✓ Đang bật' : 'Chưa bật'}
        </span>
      </div>

      {/* Thông báo kết quả */}
      {message.text && (
        <div className={`mb-4 p-3 rounded-lg text-sm ${
          message.type === 'success'
            ? 'bg-green-50 text-green-700 dark:bg-green-900/30 dark:text-green-300'
            : 'bg-red-50 text-red-600 dark:bg-red-900/30 dark:text-red-400'
        }`}>
          {message.text}
        </div>
      )}

      {/* ====== Trạng thái IDLE ====== */}
      {step === 'idle' && (
        <div>
          {!isMfaEnabled ? (
            <button
              onClick={handleStartSetup}
              disabled={loading}
              className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
            >
              {loading ? 'Đang tải...' : 'Bật MFA'}
            </button>
          ) : (
            <button
              onClick={() => setStep('disable')}
              className="w-full bg-red-500 text-white py-2 rounded-lg hover:bg-red-600 transition-colors"
            >
              Tắt MFA
            </button>
          )}
        </div>
      )}

      {/* ====== Bước SETUP: Hiển thị QR Code ====== */}
      {step === 'setup' && (
        <div>
          <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
            <strong>Bước 1:</strong> Mở <strong>Google Authenticator</strong> → nhấn dấu <strong>+</strong> → <strong>Quét mã QR</strong>
          </p>

          {/* Ảnh QR — src là Data URI trả về từ BE */}
          {qrCode && (
            <div className="flex justify-center mb-4">
              <div className="p-3 bg-white rounded-xl border-2 border-gray-200 inline-block">
                <img
                  src={qrCode}
                  alt="QR Code cho Google Authenticator"
                  className="w-48 h-48"
                />
              </div>
            </div>
          )}

          {/* Secret key để nhập tay nếu camera không đọc được */}
          <div className="mb-4 p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
            <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">
              Không quét được QR? Nhập thủ công vào app:
            </p>
            <code className="text-xs font-mono break-all text-gray-700 dark:text-gray-300">
              {secret}
            </code>
          </div>

          <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
            <strong>Bước 2:</strong> Nhập mã 6 số vừa xuất hiện trong app để xác nhận
          </p>

          {/* Form nhập OTP xác nhận */}
          <form onSubmit={handleActivate}>
            <input
              type="text"
              className="w-full px-4 py-3 border rounded-lg text-center text-xl tracking-[0.4em] font-mono dark:bg-gray-700 dark:border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3"
              placeholder="000000"
              value={code}
              onChange={(e) => {
                const val = e.target.value.replace(/\D/g, '').slice(0, 6);
                setCode(val);
              }}
              maxLength={6}
              inputMode="numeric"
              autoFocus
              required
            />
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => { setStep('idle'); setCode(''); }}
                className="flex-1 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 dark:border-gray-600 dark:hover:bg-gray-700 transition-colors"
              >
                Hủy
              </button>
              <button
                type="submit"
                disabled={loading || code.length !== 6}
                className="flex-1 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
              >
                {loading ? 'Đang xác nhận...' : 'Kích hoạt MFA'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* ====== Bước DISABLE: Xác nhận tắt MFA ====== */}
      {step === 'disable' && (
        <div>
          <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
            Nhập mã OTP từ <strong>Google Authenticator</strong> để xác nhận tắt MFA:
          </p>
          <form onSubmit={handleDisable}>
            <input
              type="text"
              className="w-full px-4 py-3 border rounded-lg text-center text-xl tracking-[0.4em] font-mono dark:bg-gray-700 dark:border-gray-600 focus:outline-none focus:ring-2 focus:ring-red-500 mb-3"
              placeholder="000000"
              value={code}
              onChange={(e) => {
                const val = e.target.value.replace(/\D/g, '').slice(0, 6);
                setCode(val);
              }}
              maxLength={6}
              inputMode="numeric"
              autoFocus
              required
            />
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => { setStep('idle'); setCode(''); }}
                className="flex-1 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 dark:border-gray-600 dark:hover:bg-gray-700 transition-colors"
              >
                Hủy
              </button>
              <button
                type="submit"
                disabled={loading || code.length !== 6}
                className="flex-1 bg-red-500 text-white py-2 rounded-lg hover:bg-red-600 disabled:opacity-50 transition-colors"
              >
                {loading ? 'Đang xử lý...' : 'Xác nhận tắt'}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default MfaSetup;
