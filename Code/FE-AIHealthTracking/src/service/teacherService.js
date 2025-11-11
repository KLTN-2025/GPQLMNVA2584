import http from '@/service/http.js';

/**
 * Chuẩn hoá gender FE ('M'|'F' hoặc 'Nam'|'Nữ') -> BE ('nam'|'nu').
 */
function toGenderBE(g) {
  if (!g) return null;
  const s = String(g).trim().toLowerCase();
  if (['f', 'female', 'nu', 'nữ', 'n'].includes(s)) return 'nu';
  return 'nam';
}

/**
 * Map TeacherResponse từ BE -> model FE dùng cho bảng.
 */
function mapTeacherRow(t) {
  return {
    id: t.id,
    name: t.fullName || '',
    email: t.email || '',
    username: t.username || '',
    phone: t.phone || '',
    avatarUrl: t.avatarUrl || '',
    gender: t.gender || '',
    specialization: t.specialization || '',
    employeeCode: t.employeeCode || '',
    dateOfBirth: t.dateOfBirth || '',
    joinDate: t.joinDate || '',
    status: t.isBlocked ? 'locked' : 'active'
  };
}

/**
 * Chuẩn hoá date (Date | string | null) -> yyyy-MM-dd | null
 */
function normalizeDateInput(v) {
  if (!v) return null;
  if (v instanceof Date) {
    if (isNaN(v.getTime())) return null;
    const dd = String(v.getDate()).padStart(2, '0');
    const mm = String(v.getMonth() + 1).padStart(2, '0');
    return `${v.getFullYear()}-${mm}-${dd}`;
  }
  const m = String(v).match(/^(\d{4}-\d{2}-\d{2})/);
  return m ? m[1] : null;
}

/**
 * Build payload đăng ký giáo viên (JSON phần text).
 */
function buildRegisterPayload(payload) {
  return {
    username: payload.username?.trim(),
    password: payload.password,
    fullName: payload.fullName?.trim(),
    email: payload.email?.trim() || '',
    phone: payload.phone?.trim() || '',
    gender: toGenderBE(payload.gender),
    specialization: payload.specialization || '',
    employeeCode: payload.employeeCode || '',
    emergencyContact: payload.emergencyContact || '',
    dateOfBirth: normalizeDateInput(payload.dateOfBirth),
    avatarUrl: payload.avatarUrl || ''
  };
}

/**
 * Lọc / sort / paginate FE-side.
 */
function applyFilters(list, params = {}) {
  let items = [...list];

  if (params.q) {
    const q = String(params.q).toLowerCase();
    items = items.filter(x =>
      (x.name || '').toLowerCase().includes(q) ||
      (x.username || '').toLowerCase().includes(q) ||
      (x.email || '').toLowerCase().includes(q) ||
      (x.phone || '').toLowerCase().includes(q)
    );
  }
  if (params.status && params.status !== 'all') {
    items = items.filter(x => x.status === params.status);
  }
  if (params.specialization) {
    const sp = String(params.specialization).toLowerCase();
    items = items.filter(x => (x.specialization || '').toLowerCase().includes(sp));
  }
  if (params.employeeCode) {
    const ec = String(params.employeeCode).toLowerCase();
    items = items.filter(x => (x.employeeCode || '').toLowerCase().includes(ec));
  }

  if (params.sort) {
    const [field, dir = 'asc'] = params.sort.split(',');
    const mul = dir.toLowerCase() === 'desc' ? -1 : 1;
    items.sort((a, b) => String(a?.[field] || '').localeCompare(String(b?.[field] || '')) * mul);
  }

  const total = items.length;
  const page = Number(params.page) || 1;
  const size = Number(params.size) || 10;
  const start = (page - 1) * size;
  const end = start + size;
  items = items.slice(start, end);

  return { items, total };
}

/**
 * GET /teachers/all
 */
export async function fetchTeachers(params = {}) {
  const res = await http.get('/teachers/all');
  const raw = Array.isArray(res?.data?.data) ? res.data.data : (Array.isArray(res?.data) ? res.data : []);
  const mapped = raw.map(mapTeacherRow);
  return applyFilters(mapped, params);
}

/**
 * Kiểm tra trùng FE-side trước khi gọi BE (username/email/employeeCode/phone).
 */
export async function checkDuplicates(fieldsCheck) {
  const { items } = await fetchTeachers({ page: 1, size: 99999 });
  const dup = {
    username: !!fieldsCheck.username && items.some(t => t.username?.toLowerCase() === fieldsCheck.username?.toLowerCase()),
    email: !!fieldsCheck.email && items.some(t => t.email?.toLowerCase() === fieldsCheck.email?.toLowerCase()),
    employeeCode: !!fieldsCheck.employeeCode && items.some(t => t.employeeCode === fieldsCheck.employeeCode),
    phone: !!fieldsCheck.phone && items.some(t => t.phone === fieldsCheck.phone)
  };
  return dup;
}

/**
 * Tạo giá trị duy nhất cho username/employeeCode/email nếu trùng (không tự sửa phone).
 */
export async function ensureUniqueFields(payload) {
  const { items } = await fetchTeachers({ page: 1, size: 99999 });
  const seen = {
    usernames: new Set(items.map(t => (t.username || '').toLowerCase())),
    emails: new Set(items.map(t => (t.email || '').toLowerCase()).filter(Boolean)),
    codes: new Set(items.map(t => t.employeeCode).filter(Boolean)),
    phones: new Set(items.map(t => t.phone).filter(Boolean))
  };

  let username = (payload.username || '').trim().toLowerCase();
  if (!username) username = 'gv.' + Date.now();
  let u = username, i = 1;
  while (seen.usernames.has(u)) {
    u = `${username}-${++i}`;
  }

  // Nếu email có và bị trùng, thêm hậu tố +N (gmail style). Nếu hệ thống không hỗ trợ, bạn có thể yêu cầu người dùng đổi tay.
  let email = (payload.email || '').trim().toLowerCase();
  if (email && seen.emails.has(email)) {
    const [local, domain] = email.split('@');
    let j = 1, e = `${local}+${j}@${domain}`;
    while (seen.emails.has(e)) e = `${local}+${++j}@${domain}`;
    email = e;
  }

  // employeeCode nếu trùng -> thêm hậu tố -N
  let code = (payload.employeeCode || '').trim();
  if (!code) {
    const y = new Date().getFullYear();
    code = `GV${y}-${Math.floor(10000 + Math.random() * 90000)}`;
  }
  let c = code, k = 1;
  while (seen.codes.has(c)) {
    c = `${code}-${++k}`;
  }

  const conflicts = {
    phone: !!payload.phone && seen.phones.has(payload.phone)
  };

  const adjusted = { ...payload, username: u, email, employeeCode: c };

  const warning =
    (u !== payload.username ? `Đã đổi username thành '${u}'. ` : '') +
    (email !== payload.email ? `Đã đổi email thành '${email}'. ` : '') +
    (c !== payload.employeeCode ? `Đã đổi mã nhân viên thành '${c}'. ` : '');

  return { payload: adjusted, conflicts, warning: warning.trim() };
}

/**
 * POST /auth/register/teacher
 */
export async function registerTeacher(payload, avatarFile) {
  const body = {
    username: payload.username?.trim(),
    password: payload.password,
    fullName: payload.fullName?.trim(),
    email: payload.email?.trim() || '',
    phone: payload.phone?.trim() || '',
    gender: toGenderBE(payload.gender),
    specialization: payload.specialization || '',
    employeeCode: payload.employeeCode || '',
    emergencyContact: payload.emergencyContact || '',
    dateOfBirth: normalizeDateInput(payload.dateOfBirth),
    avatarUrl: payload.avatarUrl || ''
  };

  if (avatarFile instanceof File || avatarFile instanceof Blob) {
    const form = new FormData();
    Object.entries(body).forEach(([k, v]) => { if (v !== undefined && v !== null) form.append(k, v); });
    form.append('avatar', avatarFile);
    const res = await http.post('/auth/register/teacher', form, { headers: { 'Content-Type': 'multipart/form-data' } });
    return mapTeacherRow(res?.data?.data || res?.data);
  }

  const res = await http.post('/auth/register/teacher', body);
  return mapTeacherRow(res?.data?.data || res?.data);
}

/**
 * Wrapper: create teacher với duplicate check + parse lỗi duplicate.
 */
export async function createOrRegisterTeacher(payload, avatarFile) {
  // Pre-check FE + auto-adjust
  const { payload: adjusted, conflicts, warning } = await ensureUniqueFields(payload);

  if (conflicts.phone) {
    // Phone thường không nên tự sửa -> yêu cầu người dùng đổi
    const err = 'Số điện thoại đã tồn tại. Vui lòng thay đổi.';
    return { teacher: null, errorDup: err, warning };
  }

  try {
    const teacher = await registerTeacher(adjusted, avatarFile);
    return { teacher, errorDup: null, warning };
  } catch (e) {
    const msg = parseDuplicateMessage(e);
    return { teacher: null, errorDup: msg, warning };
  }
}

function parseDuplicateMessage(e) {
  const raw = e?.response?.data?.message || e?.message || '';
  const m = raw.match(/Duplicate entry '([^']+)' for key '([^']+)'/);
  if (m) {
    const value = m[1]; const key = m[2];
    if (/username/i.test(key)) return `Tài khoản '${value}' đã tồn tại`;
    if (/email/i.test(key)) return `Email '${value}' đã tồn tại`;
    if (/employee/i.test(key)) return `Mã nhân viên '${value}' đã tồn tại`;
    if (/phone/i.test(key)) return `Số điện thoại '${value}' đã tồn tại`;
    return `Giá trị '${value}' đã tồn tại (khóa: ${key})`;
  }
  if (raw.toLowerCase().includes('duplicate')) {
    return 'Dữ liệu bị trùng lặp (username / email / mã nhân viên / số điện thoại).';
  }
  return raw || 'Có lỗi xảy ra';
}

/**
 * GET /teachers/export (file). Fallback CSV nếu lỗi.
 */
export async function exportTeachersExcel() {
  try {
    const res = await http.get('/teachers/export', { responseType: 'blob' });
    const blob = new Blob([res.data], { type: res.headers['content-type'] || 'application/octet-stream' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    const cd = res.headers['content-disposition'] || '';
    const m = cd.match(/filename="?([^"]+)"?/i);
    a.href = url;
    a.download = m ? m[1] : 'teachers.xlsx';
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  } catch (e) {
    // Fallback CSV
    const { items } = await fetchTeachers({ page: 1, size: 100000 });
    const header = 'name,username,email,phone,gender,specialization,employeeCode,dateOfBirth,status\n';
    const lines = items.map(t => [
      csv(t.name), csv(t.username), csv(t.email), csv(t.phone),
      csv(t.gender), csv(t.specialization), csv(t.employeeCode),
      csv(t.dateOfBirth), csv(t.status)
    ].join(',')).join('\n');
    const blob = new Blob([header + lines], { type: 'text/csv;charset=utf-8;' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'teachers.csv';
    a.click();
  }
}

/**
 * POST /teachers/import (multipart/form-data, field "file").
 */
export async function importTeachersExcel(file) {
  const form = new FormData();
  form.append('file', file);
  await http.post('/teachers/import', form, { headers: { 'Content-Type': 'multipart/form-data' } });
}

function csv(v) {
  if (v == null) return '';
  const s = String(v);
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
}

export const teacherHelpers = { toGenderBE, normalizeDateInput };
