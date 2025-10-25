const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') === 'true';
// const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

// DỮ LIỆU MẪU (mock)
function mockStaff() {
    return [
        { id: 1, name: 'vuhieutruong', email: 'tranlongvu.info@gmail.com', username: 'vuhieutruong', phone: '21356666', classes: [], role: 'Hiệu trưởng', status: 'active' },
        { id: 2, name: 'vuqlt', email: 'tranlongvu.info@gmail.com', username: 'vuqlt', phone: '213', classes: [], role: 'Quản lý trường', status: 'active' },
        { id: 3, name: 'yte123', email: 'tranlongvu.info@gmail.com', username: 'yte123', phone: '098856123', classes: [], role: 'Nhân viên y tế', status: 'active' },
        { id: 4, name: 'IRTECH GIÁO VIÊN', email: 'irtech@school.com', username: 'irtechgv', phone: '0123456788', classes: ['Panda Bear - B2', 'Global'], role: 'Giáo viên', status: 'active' },
        { id: 5, name: 'Nguyễn Thị Bích Ngọc', email: 'ngoc@school.com', username: 'ntbn', phone: '0397355723', classes: [], role: 'Giáo viên', status: 'active' },
        { id: 6, name: 'Tài khoản bị khóa', email: 'lock@school.com', username: 'locked1', phone: '0909000111', classes: [], role: 'Nhân viên', status: 'locked' },
        { id: 7, name: 'Đã xóa 1', email: 'deleted@school.com', username: 'deleted1', phone: '0909000222', classes: [], role: 'Nhân viên', status: 'deleted' },
        { id: 8, name: 'Chưa đăng nhập 1', email: 'never@school.com', username: 'never1', phone: '', classes: [], role: 'Nhân viên', status: 'neverLoggedIn' }
    ];
}

export async function fetchStaff(params = {}) {
    if (USE_MOCK) {
        const all = mockStaff();
        let items = [...all];

        if (params.status && params.status !== 'all') {
            items = items.filter((s) => s.status === params.status);
        }
        if (params.q) {
            const q = params.q.toLowerCase();
            items = items.filter((s) => s.name.toLowerCase().includes(q) || s.username.toLowerCase().includes(q) || s.email.toLowerCase().includes(q) || (s.phone ?? '').toLowerCase().includes(q));
        }

        // Paging mock
        const total = items.length;
        const page = params.page ?? 1;
        const size = params.size ?? 10;
        const start = (page - 1) * size;
        const end = start + size;
        items = items.slice(start, end);

        await new Promise((r) => setTimeout(r, 150));
        return { items, total };
    }

    // TODO (sau này): gọi API Java thật
    // const url = new URL(`${BASE_URL}/staff`);
    // if (params.q) url.searchParams.set('q', params.q);
    // if (params.status) url.searchParams.set('status', params.status);
    // url.searchParams.set('page', String(params.page ?? 1));
    // url.searchParams.set('size', String(params.size ?? 10));
    // if (params.sort) url.searchParams.set('sort', params.sort);
    // const res = await fetch(url, { headers: { Accept: 'application/json' } });
    // if (!res.ok) throw new Error(`HTTP ${res.status}`);
    // return res.json();

    throw new Error('Not implemented: integrate Java API for fetchStaff');
}

export async function exportStaffExcel() {
    if (USE_MOCK) {
        const blob = new Blob(['id,name,email\n1,Demo User,demo@school.com'], { type: 'text/csv;charset=utf-8;' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'staff.csv';
        a.click();
        return;
    }
    // TODO: tải file từ API Java
    throw new Error('Not implemented: exportStaffExcel');
}

export async function importStaffExcel(_file) {
    if (USE_MOCK) {
        await new Promise((r) => setTimeout(r, 500));
        return;
    }
    // TODO: POST multipart/form-data lên API Java
    // const form = new FormData(); form.append('file', _file);
    // await fetch(`${BASE_URL}/staff/import`, { method: 'POST', body: form });
    throw new Error('Not implemented: importStaffExcel');
}
