import http from '@/service/http.js';

/** Map BE -> FE */
function mapParentRow(p) {
    const statusKey = (p.status || '').includes('khóa') || (p.status || '').toLowerCase().includes('lock') ? 'blocked' : 'active';
    return {
        id: p.id,
        name: p.fullName || '',
        username: p.username || '',
        phone: p.phone || '',
        email: p.email || '',
        dob: p.dateOfBirth || '',
        studentNames: Array.isArray(p.studentNames) ? p.studentNames : [],
        status: p.status || (statusKey === 'blocked' ? 'Đã khóa' : 'Hoạt động'),
        statusKey,
        // optional fields from entity
        occupation: p.occupation,
        relationship: p.relationship,
        emergencyContact: p.emergencyContact,
        additionalPhone: p.additionalPhone
    };
}

/** FE filter/sort/paginate */
function applyFilters(list, params = {}) {
    let items = [...list];
    const q = (params.q || '').toLowerCase();
    if (q) {
        items = items.filter((x) => (x.name || '').toLowerCase().includes(q) || (x.phone || '').toLowerCase().includes(q) || (x.email || '').toLowerCase().includes(q));
    }
    if (params.status && params.status !== 'all') {
        items = items.filter((x) => x.statusKey === params.status);
    }
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
            return String(va).localeCompare(String(vb), 'vi') * mul;
        });
    }
    const total = items.length;
    const page = Number(params.page) || 1;
    const size = Number(params.size) || 10;
    const start = (page - 1) * size;
    const end = start + size;
    items = items.slice(start, end);

    const counts = {
        total: list.length,
        active: list.filter((x) => x.statusKey === 'active').length,
        blocked: list.filter((x) => x.statusKey === 'blocked').length
    };
    return { items, total, counts };
}

/** GET all parents */
export async function fetchParents(params = {}) {
    const res = await http.get('/parents/all');
    const raw = Array.isArray(res?.data?.data) ? res.data.data : Array.isArray(res?.data) ? res.data : [];
    const mapped = raw.map(mapParentRow);
    return applyFilters(mapped, params);
}

/** Create/Update/Delete — cập nhật endpoint cho đúng BE nếu khác */
export async function createParent(payload) {
    const res = await http.post('/parents', payload);
    return res?.data?.data || res?.data;
}
export async function updateParent(id, payload) {
    const res = await http.put(`/parents/${id}`, payload);
    return res?.data?.data || res?.data;
}
export async function deleteParents(ids = []) {
    // nếu BE không có bulk delete, loop từng id
    for (const id of ids) {
        await http.delete(`/parents/${id}`);
    }
    return true;
}
export async function lockParents(ids = []) {
    // tuỳ BE, ví dụ POST /parents/lock { ids:[] }
    try {
        const res = await http.post('/parents/lock', { ids });
        return res?.data?.data || true;
    } catch {
        // fallback: nếu không có bulk, giả định PUT /parents/{id}/lock
        for (const id of ids) await http.put(`/parents/${id}/lock`);
        return true;
    }
}
export async function unlockParents(ids = []) {
    try {
        const res = await http.post('/parents/unlock', { ids });
        return res?.data?.data || true;
    } catch {
        for (const id of ids) await http.put(`/parents/${id}/unlock`);
        return true;
    }
}

/** Export Excel; fallback CSV có BOM */
export async function exportParentsExcel() {
    try {
        const res = await http.get('/parents/export', { responseType: 'blob' });
        const blob = new Blob([res.data], { type: res.headers['content-type'] || 'application/octet-stream' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        const cd = res.headers['content-disposition'] || '';
        const m = cd.match(/filename="?([^"]+)"?/i);
        a.href = url;
        a.download = m ? m[1] : 'parents.xlsx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    } catch {
        // Fallback CSV
        const { items } = await fetchParents({ page: 1, size: 100000 });
        const header = 'id,fullName,username,phone,email,status\n';
        const csv = (v) => {
            if (v == null) return '';
            const s = String(v);
            return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
        };
        const body = items.map((i) => [i.id, i.name, i.username, i.phone, i.email, i.status].map(csv).join(',')).join('\n');
        const BOM = new Uint8Array([0xef, 0xbb, 0xbf]);
        const blob = new Blob([BOM, header + body], { type: 'text/csv;charset=utf-8;' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'parents.csv';
        a.click();
    }
}
