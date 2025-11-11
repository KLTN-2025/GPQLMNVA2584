import http from '@/service/http.js';

/* ========== Helpers ========== */

/** Map TeacherResponse (BE) -> model dùng cho bảng FE */
function mapTeacherRow(t) {
    // Lấy mảng tên lớp từ nhiều khả năng cấu trúc khác nhau
    const classes = Array.isArray(t?.classes) ? t.classes.map((c) => c?.className || c?.name).filter(Boolean) : Array.isArray(t?.classNames) ? t.classNames : [];

    // Suy luận status từ các trường phổ biến
    // - isDeleted: đã xóa
    // - isBlocked: khóa
    // - lastLoginAt (hoặc lastLogin): nếu chưa có → neverLoggedIn, ngược lại active
    let status = 'active';
    const isDeleted = t?.isDeleted === true;
    const isBlocked = t?.isBlocked === true;
    const lastLogin = t?.lastLoginAt || t?.lastLogin || null;

    if (isDeleted) status = 'deleted';
    else if (isBlocked) status = 'locked';
    else if (!lastLogin) status = 'neverLoggedIn';

    return {
        id: t.id,
        name: t.fullName || t.name || '',
        email: t.email || '',
        username: t.username || '',
        phone: t.phone || '',
        classes,
        role: t?.role?.name || t?.roleName || 'Giáo viên',
        status
    };
}

/** Lọc + tìm kiếm + sort + paginate phía FE */
function applyFiltersSortPaginate(list, params = {}) {
    let items = [...list];

    // status filter
    if (params.status && params.status !== 'all') {
        items = items.filter((x) => x.status === params.status);
    }

    // q: tìm theo name/username/email/phone
    if (params.q) {
        const q = String(params.q).toLowerCase();
        items = items.filter((x) => (x.name || '').toLowerCase().includes(q) || (x.username || '').toLowerCase().includes(q) || (x.email || '').toLowerCase().includes(q) || (x.phone || '').toLowerCase().includes(q));
    }

    // sort: "field,asc|desc"
    if (params.sort) {
        const [field, dir = 'asc'] = params.sort.split(',');
        const mul = dir.toLowerCase() === 'desc' ? -1 : 1;
        items.sort((a, b) => {
            const va = a?.[field];
            const vb = b?.[field];
            if (va == null && vb == null) return 0;
            if (va == null) return -1 * mul;
            if (vb == null) return 1 * mul;
            if (typeof va === 'number' && typeof vb === 'number') return (va - vb) * mul;
            return String(va).localeCompare(String(vb)) * mul;
        });
    }

    // paginate (FE-side)
    const total = items.length;
    const page = Number(params.page) || 1;
    const size = Number(params.size) || 10;
    const start = (page - 1) * size;
    const end = start + size;
    items = items.slice(start, end);

    return { items, total };
}

/* ========== API chính ========== */

/**
 * Lấy danh sách giáo viên từ BE rồi áp dụng filter/sort/paginate phía FE.
 * Endpoint: GET /teachers/all
 * Trả về: { items, total }
 */
export async function fetchStaff(params = {}) {
    const res = await http.get('/teachers/all');

    // Hỗ trợ cả 2 kiểu trả về: ApiResponse{data:[...]} hoặc mảng trực tiếp
    const raw = Array.isArray(res?.data?.data) ? res.data.data : Array.isArray(res?.data) ? res.data : [];

    const mapped = raw.map(mapTeacherRow);
    return applyFiltersSortPaginate(mapped, params);
}

/**
 * Xuất Excel (nếu BE có): GET /teachers/export
 * Nếu chưa có, fallback CSV đơn giản.
 */
export async function exportStaffExcel() {
    try {
        const res = await http.get('/teachers/export', { responseType: 'blob' });
        const blob = new Blob([res.data], { type: res.headers['content-type'] || 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        const cd = res.headers['content-disposition'] || '';
        const m = cd.match(/filename="?([^"]+)"?/i);
        a.href = url;
        a.download = m ? m[1] : 'teachers.xlsx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    } catch {
        // Fallback CSV
        const { items } = await fetchStaff({ page: 1, size: 10000 });
        const header = 'name,username,email,phone,role,status\n';
        const lines = items.map((i) => [csv(i.name), csv(i.username), csv(i.email), csv(i.phone), csv(i.role), csv(i.status)].join(',')).join('\n');
        const blob = new Blob([header + lines], { type: 'text/csv;charset=utf-8;' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'teachers.csv';
        a.click();
    }
}

/**
 * Import Excel (nếu BE có): POST /teachers/import (multipart/form-data, field "file")
 */
export async function importStaffExcel(file) {
    const form = new FormData();
    form.append('file', file);
    await http.post('/teachers/import', form, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
}

/* ========== Utils ========== */
function csv(v) {
    if (v == null) return '';
    const s = String(v);
    return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}
