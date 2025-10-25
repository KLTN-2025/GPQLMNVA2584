// src/plugins/sweetalert.js
import Swal from 'sweetalert2';
import 'sweetalert2/dist/sweetalert2.min.css';

export default {
    install(app) {
        // ✅ Gắn Swal gốc
        app.config.globalProperties.$swal = Swal;

        // ✅ Các hàm SweetAlert dùng chung
        app.config.globalProperties.$alertSuccess = (title = 'Thành công!', text = '') => {
            Swal.fire({
                icon: 'success',
                title,
                text,
                confirmButtonText: 'OK'
            });
        };

        app.config.globalProperties.$alertError = (title = 'Lỗi!', text = '') => {
            Swal.fire({
                icon: 'error',
                title,
                text,
                confirmButtonText: 'Thử lại'
            });
        };

        app.config.globalProperties.$alertWarning = (title = 'Cảnh báo!', text = '') => {
            Swal.fire({
                icon: 'warning',
                title,
                text,
                confirmButtonText: 'OK'
            });
        };

        app.config.globalProperties.$confirmDelete = async (title = 'Bạn có chắc muốn xóa?', text = 'Thao tác này không thể hoàn tác!') => {
            return Swal.fire({
                icon: 'question',
                title,
                text,
                showCancelButton: true,
                confirmButtonText: 'Xóa',
                cancelButtonText: 'Hủy',
                confirmButtonColor: '#d33',
                cancelButtonColor: '#3085d6'
            });
        };
    }
};
