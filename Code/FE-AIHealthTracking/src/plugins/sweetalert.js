import Swal from 'sweetalert2';
import 'sweetalert2/dist/sweetalert2.min.css';

/**
 * Tạo các mixin tái sử dụng.
 * Bạn có thể chỉnh màu trong themeColor / tailwind config của bạn.
 */
const Toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    timer: 2500,
    timerProgressBar: true,
    showConfirmButton: false,
    heightAuto: false,
    didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer);
        toast.addEventListener('mouseleave', Swal.resumeTimer);
    }
});

const Confirm = (title, text, options = {}) => {
    return Swal.fire({
        title,
        text,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: options.confirmText || 'Xác nhận',
        cancelButtonText: options.cancelText || 'Hủy',
        confirmButtonColor: options.confirmColor || '#dc2626', // red-600
        cancelButtonColor: options.cancelColor || '#64748b', // slate-500
        reverseButtons: true,
        heightAuto: false,
        ...options.swalOptions
    });
};

const showLoading = (text = 'Đang xử lý...') => {
    return Swal.fire({
        title: text,
        allowOutsideClick: false,
        showConfirmButton: false,
        didOpen: () => Swal.showLoading(),
        heightAuto: false
    });
};

const close = () => Swal.close();

/**
 * Định nghĩa plugin để inject vào app.config.globalProperties.
 */
export default {
    install(app) {
        // gắn trực tiếp Swal nếu cần toàn quyền
        app.config.globalProperties.$swal = Swal;

        // gắn các helper ngắn gọn
        app.config.globalProperties.$alert = Swal.fire;
        app.config.globalProperties.$toast = (icon, title, opts = {}) => Toast.fire({ icon, title, ...opts });

        // shortcut toast theo loại
        app.config.globalProperties.$toastSuccess = (title, o = {}) => Toast.fire({ icon: 'success', title, ...o });
        app.config.globalProperties.$toastError = (title, o = {}) => Toast.fire({ icon: 'error', title, ...o });
        app.config.globalProperties.$toastWarn = (title, o = {}) => Toast.fire({ icon: 'warning', title, ...o });
        app.config.globalProperties.$toastInfo = (title, o = {}) => Toast.fire({ icon: 'info', title, ...o });

        // confirm / loading
        app.config.globalProperties.$confirm = Confirm;
        app.config.globalProperties.$loading = showLoading;
        app.config.globalProperties.$swalClose = close;
    }
};

/**
 * (Không bắt buộc) export ra nếu bạn muốn import trực tiếp thay vì qua injection.
 */
export { Swal, Toast, Confirm, showLoading, close };
