const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') === 'true';
// const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

function mockStudents() {
  // gender: 'M' | 'F'
  // status: 'studying' | 'waiting' | 'reserved' | 'dropped' | 'graduated' | 'deleted'
  return [
    { id: 1,  code: '221028NM', name: 'Hồ Nhật Minh',       dob: '2022-10-28', gender: 'M', className: 'Koala Bear - C1',   parentName: 'Nguyễn Thị Diệu Hằng (Mẹ)',  parentPhone: '0938981611', status: 'studying' },
    { id: 2,  code: '191013QA', name: 'Dương Quỳnh Anh',    dob: '2019-10-13', gender: 'F', className: 'Polar Bear - B1',   parentName: 'Nguyễn Thị Thanh Vũ (Mẹ)',   parentPhone: '0934807579', status: 'studying' },
    { id: 3,  code: '211022CA', name: 'Phạm Châu An',       dob: '2021-10-22', gender: 'F', className: 'Global',            parentName: 'Phạm Thị Thanh Thuỳ (Mẹ)',    parentPhone: '0919139093', status: 'studying' },
    { id: 4,  code: '230708PK', name: 'Nguyễn Phúc Khang',  dob: '2023-07-08', gender: 'M', className: 'Koala Bear - C2',   parentName: 'Lê Đặng Trúc Chi (Bố)',      parentPhone: '0989689680', status: 'studying' },
    { id: 5,  code: '230701AB', name: 'Trần Gia Bảo',       dob: '2023-07-01', gender: 'M', className: 'Koala Bear - C1',   parentName: 'Trần Minh (Bố)',             parentPhone: '0912003004', status: 'studying' },
    { id: 6,  code: '230620CD', name: 'Lê Ngọc Linh',       dob: '2020-06-20', gender: 'F', className: '',                  parentName: 'Lê Hồng (Mẹ)',               parentPhone: '0912003111', status: 'waiting' },
    { id: 7,  code: '200101EF', name: 'Vũ Hồng Hạnh',       dob: '2020-01-01', gender: 'F', className: '',                  parentName: 'Vũ Hùng (Bố)',               parentPhone: '0912333111', status: 'waiting' },
    { id: 8,  code: '190501GH', name: 'Phan Hoài Nam',      dob: '2019-05-01', gender: 'M', className: 'Panda Bear - B2',   parentName: 'Phan Dũng (Bố)',             parentPhone: '0934001111', status: 'reserved' },
    { id: 9,  code: '180315IJ', name: 'Đỗ Khánh Chi',       dob: '2018-03-15', gender: 'F', className: 'Panda Bear - B3',   parentName: 'Đỗ Mai (Mẹ)',                parentPhone: '0987002222', status: 'dropped' },
    { id: 10, code: '170928KL', name: 'Phạm Hải Đăng',      dob: '2017-09-28', gender: 'M', className: 'Koala Bear - C3',   parentName: 'Phạm Quân (Bố)',             parentPhone: '0909111222', status: 'graduated' },
    { id: 11, code: '210101MN', name: 'Ngô Nhật Trường',    dob: '2021-01-01', gender: 'M', className: 'Global',            parentName: 'Ngô Trang (Mẹ)',             parentPhone: '0972004555', status: 'studying' },
    { id: 12, code: '201224OP', name: 'Bùi Gia Hân',        dob: '2020-12-24', gender: 'F', className: 'Polar Bear - B1',   parentName: 'Bùi Lan (Mẹ)',               parentPhone: '0905003333', status: 'studying' },
    { id: 13, code: '220808QR', name: 'Đặng Minh Nhật',     dob: '2022-08-08', gender: 'M', className: 'Koala Bear - C2',   parentName: 'Đặng Việt (Bố)',             parentPhone: '0912002222', status: 'studying' },
    { id: 14, code: '181231ST', name: 'Hoàng Phương Anh',   dob: '2018-12-31', gender: 'F', className: 'Panda Bear - B2',   parentName: 'Hoàng Hương (Mẹ)',           parentPhone: '0905333444', status: 'deleted' },
    { id: 15, code: '201001UV', name: 'Tôn Đức Phúc',       dob: '2020-10-01', gender: 'M', className: 'Global',            parentName: 'Tôn Thảo (Mẹ)',              parentPhone: '0905666777', status: 'studying' },
  ];
}

export async function fetchStudents(params = {}) {
  if (USE_MOCK) {
    let items = mockStudents();

    // status filter
    if (params.status && params.status !== 'all') {
      items = items.filter(s => s.status === params.status);
    }

    // quick filters (year/grade/group/system) – demo: no-op for now
    // if (params.year) { ... }
    // if (params.grade) { ... }

    // column filters / search
    if (params.code) {
      const q = params.code.toLowerCase();
      items = items.filter(s => s.code.toLowerCase().includes(q));
    }
    if (params.name) {
      const q = params.name.toLowerCase();
      items = items.filter(s => s.name.toLowerCase().includes(q));
    }
    if (params.className) {
      const q = params.className.toLowerCase();
      items = items.filter(s => (s.className || '').toLowerCase().includes(q));
    }
    if (params.parentName) {
      const q = params.parentName.toLowerCase();
      items = items.filter(s => (s.parentName || '').toLowerCase().includes(q));
    }

    // sort (field, order)
    if (params.sort) {
      const [field, dir] = params.sort.split(',');
      items.sort((a, b) => {
        const va = (a[field] || '').toString().toLowerCase();
        const vb = (b[field] || '').toString().toLowerCase();
        if (va < vb) return dir === 'desc' ? 1 : -1;
        if (va > vb) return dir === 'desc' ? -1 : 1;
        return 0;
      });
    }

    // paging
    const total = items.length;
    const page = params.page ?? 1;
    const size = params.size ?? 10;
    const start = (page - 1) * size;
    const end = start + size;
    items = items.slice(start, end);

    await new Promise(r => setTimeout(r, 120));
    return { items, total };
  }

  // TODO: gọi API Java thật sau này
  throw new Error('Not implemented: integrate Java API for fetchStudents');
}

export async function exportStudentsExcel() {
  if (USE_MOCK) {
    const blob = new Blob(['code,name,class\n221028NM,Hồ Nhật Minh,Koala Bear - C1'], { type: 'text/csv;charset=utf-8;' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'students.csv';
    a.click();
    return;
  }
  throw new Error('Not implemented: exportStudentsExcel');
}

export async function importStudentsExcel(_file) {
  if (USE_MOCK) {
    await new Promise(r => setTimeout(r, 400));
    return;
  }
  throw new Error('Not implemented: importStudentsExcel');
}
