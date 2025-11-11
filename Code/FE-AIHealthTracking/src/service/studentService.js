import http from '@/service/http.js';
import * as XLSX from 'xlsx'; // npm i xlsx

/* ---------------- Helpers chuyển đổi ---------------- */
function toGenderChar(g) {
    if (!g) return 'M';
    const s = String(g).toUpperCase();
    if (s.startsWith('F') || s === 'NỮ') return 'F';
    return 'M';
}

/* Chuẩn hoá ngày: nhận yyyy-MM-dd hoặc dd/MM/yyyy -> yyyy-MM-dd; sai trả '' */
function normalizeDate(s) {
    if (!s) return '';
    s = String(s).trim();
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
        const d = new Date(s);
        return isNaN(d.getTime()) ? '' : s;
    }
    if (/^\d{2}\/\d{2}\/\d{4}$/.test(s)) {
        const [dd, mm, yyyy] = s.split('/');
        const iso = `${yyyy}-${mm}-${dd}`;
        const d = new Date(iso);
        return isNaN(d.getTime()) ? '' : iso;
    }
    return '';
}

/* Chuẩn hoá giới tính nhiều dạng -> 'M' | 'F' hoặc '' nếu không hợp lệ */
function normalizeGender(g) {
    if (!g) return '';
    const s = String(g).trim().toLowerCase();
    if (['m', 'nam', 'male'].includes(s)) return 'M';
    if (['f', 'nu', 'nữ', 'female'].includes(s)) return 'F';
    return '';
}

/* ---------------- Parent cache & helpers (dùng chung cho bảng + modal) ---------------- */
const parentCache = {
    loaded: false,
    map: new Map() // id -> { fullName, phone, email }
};

async function loadParentsAllToCache() {
    if (parentCache.loaded) return parentCache.map;
    try {
        const res = await http.get('/parents/all'); // baseURL should be /api/v1
        const raw = Array.isArray(res?.data?.data) ? res.data.data : Array.isArray(res?.data) ? res.data : [];
        for (const p of raw) {
            const id = p.id ?? p.parentId;
            if (id == null) continue;
            const fullName = p.fullName || p.username || p.name || `PH ${id}`;
            const phone = p.phone || p.parentPhone || p.additionalPhone || p.additional_phone || '';
            const email = p.email || '';
            parentCache.map.set(Number(id), { fullName, phone, email });
        }
        parentCache.loaded = true;
    } catch (e) {
        console.warn('[studentService] loadParentsAllToCache failed:', e?.message || e);
    }
    return parentCache.map;
}

// Build dropdown options [{ value, label }] từ cache phụ huynh
export async function fetchParentOptionsLikeStudentService() {
    const map = await loadParentsAllToCache();
    const opts = [];
    for (const [id, p] of map.entries()) {
        const label = p.phone ? `${p.fullName} (${p.phone})` : p.fullName;
        opts.push({ value: id, label });
    }
    opts.sort((a, b) => a.label.localeCompare(b.label, 'vi'));
    return opts;
}

// Bổ sung parentName/parentPhone theo parentId nếu thiếu
async function enrichStudentsWithParents(items) {
    if (!items?.length) return items;
    const needs = items.some((x) => !x.parentName || !x.parentPhone);
    if (!needs) return items;
    const map = await loadParentsAllToCache();
    for (const it of items) {
        const pid = Number(it.parentId);
        if (pid && map.has(pid)) {
            const p = map.get(pid);
            if (!it.parentName) it.parentName = p.fullName || it.parentName;
            if (!it.parentPhone) it.parentPhone = p.phone || it.parentPhone;
        }
    }
    return items;
}

/* ---------------- Map từ BE -> model bảng ---------------- */
function mapStudentRow(s) {
    // className và parent lấy linh hoạt nhiều field
    const className = s.className || s.clazz?.className || s.class?.className || '';
    const parentId = s.parentId ?? s.parent_id ?? s.parent?.id ?? null;
    const parentName = s.parentName || s.parent?.fullName || s.parent?.name || '';
    const parentPhone = s.parentPhone || s.parent?.phone || s.parent?.parentPhone || '';

    return {
        id: s.id,
        code: s.studentCode || '',
        name: s.fullName || '',
        dob: s.dateOfBirth || '',
        gender: toGenderChar(s.gender),
        className,
        parentId,
        parentName,
        parentPhone,
        status: s.status || 'studying',
        username: s.username,
        email: s.email,
        phone: s.phone,
        avatarUrl: s.avatarUrl,
        address: s.address,
        enrollmentDate: s.enrollmentDate,
        healthNotes: s.healthNotes
    };
}

export async function createStudent(payload) {
    try {
        // theo yêu cầu của bạn: /students/create
        const res = await http.post('/students/create', payload);
        return res?.data?.data || res?.data;
    } catch (err) {
        throw new Error(err?.response?.data?.message || err?.message || 'Tạo học sinh thất bại');
    }
}

/* ---------------- Lỗi ---------------- */
function extractErrorMessage(err, fallback = 'Lỗi không xác định') {
    return err?.response?.data?.message || err?.response?.data?.error || err?.message || fallback;
}

/* ---------------- Lọc/sort/paginate FE-side ---------------- */
function applyFiltersSortPaginate(list, params = {}) {
    let items = [...list];
    if (params.status && params.status !== 'all') items = items.filter((x) => x.status === params.status);
    if (params.code) {
        const q = params.code.trim().toLowerCase();
        items = items.filter((x) => (x.code || '').toLowerCase().includes(q));
    }
    if (params.name) {
        const q = params.name.trim().toLowerCase();
        items = items.filter((x) => (x.name || '').toLowerCase().includes(q));
    }
    if (params.className) {
        const q = params.className.trim().toLowerCase();
        items = items.filter((x) => (x.className || '').toLowerCase().includes(q));
    }
    if (params.parentName) {
        const q = params.parentName.trim().toLowerCase();
        items = items.filter((x) => (x.parentName || '').toLowerCase().includes(q));
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
    return { items, total };
}

/* ---------------- API hiện có (FE-side filter) ---------------- */
export async function fetchStudents(params = {}) {
    try {
        const res = await http.get('/students/all');
        const raw = Array.isArray(res?.data?.data) ? res.data.data : Array.isArray(res?.data) ? res.data : [];
        let mapped = raw.map(mapStudentRow);
        mapped = await enrichStudentsWithParents(mapped);
        return applyFiltersSortPaginate(mapped, params);
    } catch (err) {
        throw new Error(extractErrorMessage(err, 'Không thể tải danh sách học sinh'));
    }
}

/* Import qua BE */
export async function importStudentsExcel(file) {
    if (!file) throw new Error('Thiếu file import');
    const form = new FormData();
    form.append('file', file);
    try {
        const res = await http.post('/students/import', form, { headers: { 'Content-Type': 'multipart/form-data' } });
        return res?.data?.message || true;
    } catch (err) {
        throw new Error(extractErrorMessage(err, 'Import Excel thất bại'));
    }
}

/* Tải template từ BE */
export async function downloadStudentsTemplate() {
    try {
        const res = await http.get('/students/template', { responseType: 'blob' });
        const blob = new Blob([res.data], { type: res.headers['content-type'] || 'application/octet-stream' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        const cd = res.headers['content-disposition'] || '';
        const m = cd.match(/filename="?([^"]+)"?/i);
        a.href = url;
        a.download = m ? m[1] : 'students_template.xlsx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    } catch (err) {
        throw new Error(extractErrorMessage(err, 'Tải mẫu thất bại'));
    }
}

/* Xuất file; fallback CSV có BOM */
export async function exportStudentsExcel() {
    try {
        const res = await http.get('/students/export', { responseType: 'blob' });
        const blob = new Blob([res.data], { type: res.headers['content-type'] || 'application/octet-stream' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        const cd = res.headers['content-disposition'] || '';
        theDownload(a, url, (cd.match(/filename="?([^"]+)"?/i) || [])[1] || 'students.xlsx');
    } catch (err) {
        const { items } = await fetchStudents({ page: 1, size: 10000 });
        const header = 'code,name,className,parentName\n';
        const rows = items.map((r) => `${csv(r.code)},${csv(r.name)},${csv(r.className)},${csv(r.parentName)}`).join('\n');
        const BOM = new Uint8Array([0xef, 0xbb, 0xbf]);
        const blob = new Blob([BOM, header + rows], { type: 'text/csv;charset=utf-8;' });
        const a = document.createElement('a');
        const url = URL.createObjectURL(blob);
        theDownload(a, url, 'students.csv');
    }
}
function theDownload(a, url, name) {
    a.href = url;
    a.download = name;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
}

/* ---------------- CSV escape ---------------- */
function csv(v) {
    if (v == null) return '';
    const s = String(v);
    return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}

/* ---------------- DELETE: SweetAlert-friendly (timeout + bulk song song) ---------------- */
export async function deleteStudent(id, { timeoutMs = 12000 } = {}) {
    if (!id) throw new Error('Thiếu id học sinh');
    try {
        await http.delete(`/students/delete/${id}`, {
            timeout: timeoutMs, // axios timeout
            validateStatus: (s) => (s >= 200 && s < 300) || s === 204
        });
        return true;
    } catch (err) {
        const msg =
            err?.code === 'ECONNABORTED' ||
            String(err?.message || '')
                .toLowerCase()
                .includes('timeout')
                ? 'Hết thời gian chờ (timeout) khi xóa'
                : err?.response?.data?.message || err?.message || 'Xóa học sinh thất bại';
        throw new Error(msg);
    }
}

// Xóa nhiều học sinh: chạy song song và trả thống kê
export async function deleteStudents(ids = [], opts = {}) {
    if (!Array.isArray(ids) || !ids.length) return { ok: 0, fail: 0, errors: [] };
    const results = await Promise.allSettled(ids.map((id) => deleteStudent(id, opts)));
    const summary = { ok: 0, fail: 0, errors: [] };
    results.forEach((r, i) => {
        if (r.status === 'fulfilled') summary.ok += 1;
        else (summary.fail += 1), summary.errors.push({ id: ids[i], message: r.reason?.message || 'Xóa thất bại' });
    });
    return summary;
}

/* ---------------- FE xử lý import Excel -> gọi nhiều POST /students ---------------- */
export async function bulkImportStudentsFromFile(file, options = {}) {
    const {
        requiredHeaders = ['studentCode', 'fullName', 'dateOfBirth', 'gender', 'className', 'parentName', 'parentPhone', 'parentEmail', 'address'],
        maxPreview = 20000,
        chunkSize = 20,
        maxConcurrency = 5,
        onProgress = () => {},
        autoGenerateCode = true
    } = options;

    if (!file) throw new Error('Thiếu file');

    // 1) Đọc Excel
    let workbook;
    try {
        const buf = await file.arrayBuffer();
        workbook = XLSX.read(buf, { type: 'array' });
    } catch {
        throw new Error('Không đọc được file Excel');
    }

    const sheetName = workbook.SheetNames[0];
    if (!sheetName) throw new Error('Không có sheet trong file');
    const ws = workbook.Sheets[sheetName];
    const json = XLSX.utils.sheet_to_json(ws, { defval: '', raw: false });
    if (!json.length) throw new Error('File rỗng');

    // 2) Kiểm tra header
    const headers = Object.keys(json[0]).map((h) => h.trim());
    const missing = requiredHeaders.filter((h) => !headers.includes(h));
    if (missing.length) throw new Error('Thiếu cột: ' + missing.join(', '));

    // 3) Lấy danh sách hiện có để chống trùng
    let existing = [];
    try {
        const all = await fetchStudents({ status: 'all', page: 1, size: 99999 });
        existing = all.items;
    } catch {
        existing = [];
    }
    const codeSet = new Set(existing.map((s) => s.code).filter(Boolean));
    const parentPhoneSet = new Set(existing.map((s) => s.parentPhone).filter(Boolean));
    const emailSet = new Set(existing.map((s) => s.email).filter(Boolean));

    // 4) Validate & build payload
    const rows = json.slice(0, maxPreview).map((r, i) => {
        const rowErrors = [];

        let studentCode = (r.studentCode || '').trim();
        if (!studentCode && autoGenerateCode) {
            studentCode = 'HS' + Date.now().toString().slice(-6) + (Math.floor(Math.random() * 900) + 100);
        }
        if (!studentCode) rowErrors.push('Thiếu studentCode');

        const fullName = (r.fullName || '').trim();
        if (!fullName) rowErrors.push('Thiếu fullName');

        const rawDob = (r.dateOfBirth || '').trim();
        const dateOfBirth = normalizeDate(rawDob);
        if (!rawDob) rowErrors.push('Thiếu dateOfBirth');
        else if (!dateOfBirth) rowErrors.push('dateOfBirth không hợp lệ');

        const gender = normalizeGender(r.gender);
        if (!gender) rowErrors.push('gender không hợp lệ (M|F|Nam|Nữ)');

        const className = (r.className || '').trim();
        if (!className) rowErrors.push('Thiếu className');

        const parentName = (r.parentName || '').trim();
        if (!parentName) rowErrors.push('Thiếu parentName');

        const parentPhone = (r.parentPhone || '').trim();
        if (!parentPhone) rowErrors.push('Thiếu parentPhone');
        else if (!/^[0-9+()\-\s]{6,20}$/.test(parentPhone)) rowErrors.push('parentPhone sai định dạng');

        const parentEmail = (r.parentEmail || '').trim();
        if (parentEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(parentEmail)) rowErrors.push('parentEmail không hợp lệ');

        const address = (r.address || '').trim();

        // Trùng so với DB hiện có
        if (studentCode && codeSet.has(studentCode)) rowErrors.push('studentCode trùng');
        if (parentPhone && parentPhoneSet.has(parentPhone)) rowErrors.push('parentPhone trùng');
        if (parentEmail && emailSet.has(parentEmail)) rowErrors.push('parentEmail trùng');

        return {
            index: i,
            rowNumber: i + 2,
            errors: rowErrors,
            payload: {
                studentCode,
                fullName,
                dateOfBirth,
                gender,
                className,
                parentName,
                parentPhone,
                parentEmail,
                address
            }
        };
    });

    const validRows = rows.filter((r) => r.errors.length === 0);
    const invalidRows = rows.filter((r) => r.errors.length > 0);

    // 5) Chunk POST /students
    const report = {
        total: rows.length,
        valid: validRows.length,
        invalid: invalidRows.length,
        inserted: 0,
        failed: 0,
        skipped: invalidRows.length,
        errors: []
    };

    const chunks = [];
    for (let i = 0; i < validRows.length; i += chunkSize || 20) {
        chunks.push(validRows.slice(i, i + (chunkSize || 20)));
    }

    let processed = 0;

    async function runChunk(chunk) {
        const promises = chunk.map(async (r) => {
            try {
                await http.post('/students', r.payload);
                if (r.payload.studentCode) codeSet.add(r.payload.studentCode);
                if (r.payload.parentPhone) parentPhoneSet.add(r.payload.parentPhone);
                if (r.payload.parentEmail) emailSet.add(r.payload.parentEmail);
                report.inserted += 1;
            } catch (e) {
                report.failed += 1;
                report.errors.push(`Row ${r.rowNumber}: ${extractErrorMessage(e, 'Lỗi tạo')}`);
            } finally {
                processed += 1;
                (onProgress || (() => {}))({
                    processed,
                    total: validRows.length,
                    percent: Math.round((processed * 100) / (validRows.length || 1))
                });
            }
        });
        await Promise.all(promises);
    }

    // Scheduler giới hạn số chunk chạy song song
    let active = 0;
    let idx = 0;
    await new Promise((resolve) => {
        function next() {
            if (idx >= chunks.length && active === 0) return resolve();
            while (active < (maxConcurrency || 5) && idx < chunks.length) {
                const c = chunks[idx++];
                active++;
                runChunk(c).then(() => {
                    active--;
                    next();
                });
            }
        }
        next();
    });

    return { report, rows, validRows, invalidRows };
}
