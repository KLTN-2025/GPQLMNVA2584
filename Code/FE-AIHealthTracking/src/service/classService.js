import http from '@/service/http.js';

/** Chuẩn hoá 1 record lớp từ BE -> FE */
function mapClassRow(c) {
    return {
        id: c.id ?? c.classId,
        name: c.className || c.name || '',
        code: c.classCode || c.code || '',
        gradeName: c.grade || c.gradeName || '',
        roomName: c.roomNumber || c.roomName || '',
        academicYear: c.academicYear || c.year || '',
        teacherName: c.teacherName || c.homeroomTeacher || '',
        studentCurrent: c.studentCurrent ?? 0,
        studentCapacity: c.studentCapacity ?? null,
        status: c.status || 'active'
    };
}

function csv(v) {
    if (v == null) return '';
    const s = String(v);
    return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}

function applyFilters(list, params = {}) {
    let items = [...list];
    if (params.year) items = items.filter((x) => x.academicYear === params.year);
    if (params.name) {
        const q = params.name.toLowerCase();
        items = items.filter((x) => (x.name || '').toLowerCase().includes(q));
    }
    if (params.roomName) {
        const q = params.roomName.toLowerCase();
        items = items.filter((x) => (x.roomName || '').toLowerCase().includes(q));
    }
    if (params.gradeName) {
        const q = params.gradeName.toLowerCase();
        items = items.filter((x) => (x.gradeName || '').toLowerCase().includes(q));
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
            return String(va).localeCompare(String(vb), 'vi') * mul;
        });
    }
    const total = items.length;
    const page = Number(params.page) || 1;
    const size = Number(params.size) || 10;
    const start = (page - 1) * size;
    const end = start + size;
    items = items.slice(start, end);
    return { items, total };
}

/** Lấy danh sách lớp */
export async function fetchClasses(params = {}) {
    try {
        const res = await http.get('/classes/all'); // baseURL đã có /api/v1
        const raw = Array.isArray(res?.data?.data) ? res.data.data : Array.isArray(res?.data) ? res.data : [];
        const mapped = raw.map(mapClassRow);
        return applyFilters(mapped, params);
    } catch (e) {
        throw new Error(e?.response?.data?.message || e?.message || 'Không tải được danh sách lớp');
    }
}

/** Export Excel; fallback CSV (BOM để không lỗi dấu) */
export async function exportClassesExcel() {
    try {
        const res = await http.get('/classes/export', { responseType: 'blob' });
        const blob = new Blob([res.data], { type: res.headers['content-type'] || 'application/octet-stream' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        const cd = res.headers['content-disposition'] || '';
        const m = cd.match(/filename="?([^"]+)"?/i);
        a.href = url;
        a.download = m ? m[1] : 'classes.xlsx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    } catch {
        const { items } = await fetchClasses({ page: 1, size: 10000 });
        const header = 'id,name,code,gradeName,roomName,academicYear,teacherName\n';
        const body = items.map((i) => [i.id, i.name, i.code, i.gradeName, i.roomName, i.academicYear, i.teacherName].map(csv).join(',')).join('\n');
        const BOM = new Uint8Array([0xef, 0xbb, 0xbf]);
        const blob = new Blob([BOM, header + body], { type: 'text/csv;charset=utf-8;' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'classes.csv';
        a.click();
    }
}

/** Map lớp -> option dropdown */
function mapClassOption(c) {
    const id = c.id ?? c.classId;
    if (id == null) return null;
    const className = c.className || c.name || (c.code ? `Lớp ${c.code}` : `Lớp ${id}`);
    const grade = c.grade || c.gradeName || '';
    const room = c.roomNumber || c.roomName || '';
    const year = c.academicYear || c.year || '';
    let label = className;
    if (grade) label += ` - ${grade}`;
    if (room) label += ` (${room})`;
    if (year) label += ` • ${year}`;
    return { value: id, label };
}

/** Lấy options cho Dropdown lớp */
export async function fetchClassOptions(params = {}) {
    const { year } = params;
    let raw = [];
    try {
        const res = await http.get('/classes/all');
        raw = Array.isArray(res?.data?.data) ? res.data.data : Array.isArray(res?.data) ? res.data : [];
    } catch (e) {
        // fallback
        try {
            const res2 = await http.get('/classes');
            raw = Array.isArray(res2?.data?.data) ? res2.data.data : Array.isArray(res2?.data) ? res2.data : [];
        } catch (e2) {
            console.error('fetchClassOptions lỗi', e2);
            return [];
        }
    }
    if (year) raw = raw.filter((c) => (c.academicYear || c.year || '') === year);
    const opts = raw.map(mapClassOption).filter(Boolean);
    opts.sort((a, b) => a.label.localeCompare(b.label, 'vi'));
    return opts;
}
