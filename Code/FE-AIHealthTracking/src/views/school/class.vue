<script setup>
import { ref, onMounted, watch } from 'vue';
import '@fortawesome/fontawesome-free/css/all.min.css';

import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Avatar from 'primevue/avatar';
import Menu from 'primevue/menu';
import Paginator from 'primevue/paginator';

import { fetchClasses, exportClassesExcel } from '@/service/classService.js';

/* Top filters */
const years = ref([
    { label: '2025 - 2026', value: '2025-2026' },
    { label: '2024 - 2025', value: '2024-2025' },
    { label: '2023 - 2024', value: '2023-2024' }
]);
const selectedYear = ref(years.value[0]);

const systems = ref([
    { label: 'Chọn hệ lớp', value: '' },
    { label: 'Polar', value: 'Polar' },
    { label: 'Sun Bear', value: 'Sun Bear' },
    { label: 'Koala', value: 'Koala' },
    { label: 'Global', value: 'Global' }
]);
const selectedSystem = ref(systems.value[0]);

/* Header filters */
const fName = ref('');
const fRoom = ref('');
const fGrade = ref('');

/* Table state */
const loading = ref(false);
const rows = ref([]);
const totalRecords = ref(0);
const page = ref(1);
const size = ref(10);
const sortField = ref(''); // name | roomName | gradeName | studentCurrent
const sortOrder = ref(1);

/* Row menu */
const rowMenu = ref();
const activeRow = ref(null);
const rowMenuItems = ref([
    { label: 'Xem chi tiết', icon: 'fa-regular fa-eye', tone: 'primary', sub: 'Thông tin lớp', command: () => {} },
    { label: 'Sửa lớp', icon: 'fa-regular fa-pen-to-square', tone: 'info', sub: 'Tên, sĩ số, ...', command: () => {} },
    { label: 'Phân công giáo viên', icon: 'fa-solid fa-user-check', tone: 'warn', sub: 'Thêm/bớt giáo viên', command: () => {} },
    { separator: true },
    { label: 'Đóng lớp', icon: 'fa-solid fa-door-closed', tone: 'warn', sub: 'Tạm ngưng tuyển', command: () => {} },
    { label: 'Xóa lớp', icon: 'fa-regular fa-trash-can', tone: 'danger', sub: 'Không thể hoàn tác', command: () => {} }
]);
function openRowMenu(e, row) {
    activeRow.value = row;
    rowMenu.value.toggle(e);
}

/* Load */
async function load() {
    loading.value = true;
    try {
        const sort = sortField.value ? `${sortField.value},${sortOrder.value === -1 ? 'desc' : 'asc'}` : undefined;
        const { items, total } = await fetchClasses({
            year: selectedYear.value?.value, // so sánh academicYear
            name: fName.value || undefined, // nếu sau này bạn thêm ô tìm tên
            roomName: fRoom.value || undefined,
            gradeName: fGrade.value || undefined,
            page: page.value,
            size: size.value,
            sort
        });
        rows.value = items;
        totalRecords.value = total;
    } finally {
        loading.value = false;
    }
}

/* Events */
function onSort(field) {
    if (sortField.value === field) sortOrder.value = sortOrder.value === 1 ? -1 : 1;
    else {
        sortField.value = field;
        sortOrder.value = 1;
    }
    page.value = 1;
    load();
}
function onChangePage(e) {
    page.value = e.page + 1;
    size.value = e.rows;
    load();
}
async function onExport() {
    await exportClassesExcel();
}

/* Debounce header filters */
let t;
function debounce(fn, ms = 250) {
    clearTimeout(t);
    t = setTimeout(fn, ms);
}
watch([fName, fRoom, fGrade, selectedYear, selectedSystem], () =>
    debounce(() => {
        page.value = 1;
        load();
    }, 300)
);

onMounted(load);

/* Helpers */
function ageDisplay(min, max) {
    if (min == null && max == null) return '-';
    if (min != null && max != null) return `${min} - ${max}`;
    return `${min ?? max}+`;
}
</script>

<template>
    <div class="space-y-4 px-4 md:px-6 lg:px-8 py-5">
        <!-- Header -->
        <div class="rounded-xl bg-slate-50 border border-slate-200 px-4 py-3">
            <div class="flex items-center justify-between">
                <h1 class="text-xl font-semibold text-slate-800">Lớp học</h1>
                <Button class="!bg-green-600 !border-0 !text-white" icon="fa-solid fa-file-arrow-down mr-2" label="Xuất excel" @click="onExport" />
            </div>
        </div>

        <!-- Top filters -->
        <div class="rounded-xl border border-slate-200 overflow-hidden bg-white">
            <DataTable :value="rows" :loading="loading" dataKey="id" responsiveLayout="scroll" :rowHover="true" class="p-datatable-sm">
                <Column header="#" :body="(_, opt) => opt.rowIndex + 1" headerStyle="width: 4rem" />

                <!-- Tên lớp + mã -->
                <Column>
                    <template #header>
                        <div class="header-filter nowrap">
                            <span class="font-semibold">Tên lớp</span>
                            <button class="sort-btn" @click="onSort('name')" title="Sắp xếp theo tên">
                                <i class="fa-solid fa-up-down"></i>
                            </button>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <div class="class-cell">
                            <div class="class-title" :title="data.name">{{ data.name || '-' }}</div>
                            <div v-if="data.code" class="class-code" :title="data.code">{{ data.code }}</div>
                        </div>
                    </template>
                </Column>

                <!-- Phòng học -->
                <Column>
                    <template #header>
                        <div class="header-filter nowrap">
                            <span class="font-semibold">Phòng học</span>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <span class="ellipsis" :title="data.roomName">{{ data.roomName || '-' }}</span>
                    </template>
                </Column>

                <!-- Khối lớp -->
                <Column>
                    <template #header>
                        <div class="header-filter nowrap">
                            <span class="font-semibold">Khối Lớp</span>
                            <button class="sort-btn" @click="onSort('gradeName')" title="Sắp xếp theo khối">
                                <i class="fa-solid fa-up-down"></i>
                            </button>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <span class="ellipsis" :title="data.gradeName">{{ data.gradeName || '-' }}</span>
                    </template>
                </Column>

                <!-- Số HS -->
                <Column header="Số HS (0)" headerStyle="width:120px">
                    <template #body="{ data }">
                        <span class="font-semibold">{{ data.studentCurrent }}</span
                        >/<span>{{ data.studentCapacity ?? '-' }}</span>
                    </template>
                </Column>

                <!-- Độ tuổi -->
                <Column header="Độ tuổi" headerStyle="width:100px">
                    <template #body="{ data }"> - </template>
                </Column>

                <!-- Giáo viên (từ teacherName) -->
                <Column header="Giáo viên" headerStyle="width:180px">
                    <template #body="{ data }">
                        <span class="ellipsis" :title="data.teacherName">{{ data.teacherName || 'Chưa có giáo viên' }}</span>
                    </template>
                </Column>

                <!-- Hệ đào tạo (chưa có -> '-') -->
                <Column header="Hệ đào tạo">
                    <template #body> - </template>
                </Column>

                <!-- Trạng thái -->
                <Column header="Trạng thái" headerStyle="width:160px">
                    <template #body="{ data }">
                        <span class="status-badge status--active">Đang hoạt động</span>
                    </template>
                </Column>

                <!-- Hành động -->
                <Column header="Hành động" headerStyle="width:64px; text-align:right;" bodyStyle="text-align:right;">
                    <template #body="{ data }">
                        <Button icon="fa-solid fa-ellipsis-vertical" class="!bg-transparent !border-0 !text-slate-600 hover:!bg-slate-100" @click="(e) => openRowMenu(e, data)" />
                    </template>
                </Column>
            </DataTable>

            <div class="border-t border-slate-200">
                <Paginator :rows="size" :totalRecords="totalRecords" :rowsPerPageOptions="[10, 20, 50]" @page="onChangePage" />
            </div>
        </div>

        <!-- Row menu đẹp -->
        <Menu ref="rowMenu" :model="rowMenuItems" :popup="true" appendTo="body" :pt="{ menu: { class: 'rowmenu-panel' } }">
            <template #item="{ item, props }">
                <div v-if="item.separator" class="rowmenu-sep"></div>
                <a
                    v-else
                    v-bind="props.action"
                    class="menu-item"
                    :class="{
                        'menu-item--danger': item.tone === 'danger',
                        'menu-item--warn': item.tone === 'warn',
                        'menu-item--info': item.tone === 'info',
                        'menu-item--primary': item.tone === 'primary'
                    }"
                >
                    <i :class="['mr-3 w-4 text-center', item.icon]"></i>
                    <div class="flex-1 min-w-0">
                        <div class="menu-item__label truncate">{{ item.label }}</div>
                        <div v-if="item.sub" class="menu-item__sub truncate">{{ item.sub }}</div>
                    </div>
                </a>
            </template>
        </Menu>
    </div>
</template>

<style scoped>
.header-filter {
    display: flex;
    align-items: center;
    gap: 8px;
}
/* Header vẫn không wrap */
:deep(.p-datatable .p-datatable-thead > tr > th) {
    white-space: nowrap;
}

/* CHO PHÉP TÊN LỚP XUỐNG HÀNG + HIỂN THỊ HẾT */
.class-cell {
    display: block;
    line-height: 1.25;
    /* Tuỳ chọn: đặt max-width nếu muốn giới hạn độ rộng cột để wrap đẹp hơn */
    /* max-width: 480px; */
}
.class-title {
    font-weight: 600;
    color: #075985; /* xanh nhẹ */
    white-space: normal !important;
    overflow-wrap: anywhere;
    word-break: break-word;
}
.class-code {
    font-size: 12px;
    color: #64748b; /* xám nhạt */
    margin-top: 2px;
}

/* Nút sort (giữ nguyên như bạn gửi) */
.sort-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    border-radius: 8px;
    color: #64748b;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
}
.sort-btn:hover {
    background: #f1f5f9;
}

/* Trạng thái */
.status-badge {
    display: inline-flex;
    align-items: center;
    height: 28px;
    padding: 0 10px;
    border-radius: 9999px;
    font-weight: 600;
    font-size: 13px;
    border: 1px solid transparent;
}
.status--active {
    background: #e6f0ff;
    color: #1d4ed8;
    border-color: #bfdbfe;
}
.status--inactive {
    background: #fee2e2;
    color: #b91c1c;
    border-color: #fecaca;
}

/* Row menu */
:deep(.rowmenu-panel) {
    padding: 8px;
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    box-shadow:
        0 10px 15px -3px rgba(0, 0, 0, 0.1),
        0 4px 6px -4px rgba(0, 0, 0, 0.1);
    min-width: 240px;
}
.rowmenu-sep {
    height: 1px;
    background: #e5e7eb;
    margin: 6px 4px;
}
.menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 10px;
    color: #0f172a;
    text-decoration: none;
}
.menu-item:hover {
    background: #f8fafc;
}
.menu-item__label {
    font-weight: 600;
    color: #0f172a;
    line-height: 1.1;
}
.menu-item__sub {
    font-size: 12px;
    color: #64748b;
}
.menu-item--primary i {
    color: #2563eb;
}
.menu-item--info i {
    color: #0ea5e9;
}
.menu-item--warn i {
    color: #f59e0b;
}
.menu-item--danger i {
    color: #dc2626;
}
:deep(.p-datatable .p-datatable-thead > tr > th) {
    white-space: nowrap;
}

/* Header filter container cũng không wrap */
.header-filter.nowrap {
    white-space: nowrap;
}

/* Ô không xuống dòng + cắt dấu … nếu tràn */
.cell-nowrap {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* Dùng cho text bên trong cell nếu cần cắt độc lập */
.ellipsis {
    display: inline-block;
    max-width: 100%;
    vertical-align: bottom;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* Nút sort (giữ nguyên nếu bạn đã có) */
.sort-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    border-radius: 8px;
    color: #64748b;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
}
.sort-btn:hover {
    background: #f1f5f9;
}

/* Badge trạng thái (giữ nguyên) */
.status-badge {
    display: inline-flex;
    align-items: center;
    height: 28px;
    padding: 0 10px;
    border-radius: 9999px;
    font-weight: 600;
    font-size: 13px;
    border: 1px solid transparent;
}
.status--active {
    background: #e6f0ff;
    color: #1d4ed8;
    border-color: #bfdbfe;
}
.status--inactive {
    background: #fee2e2;
    color: #b91c1c;
    border-color: #fecaca;
}
</style>
