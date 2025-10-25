<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import '@fortawesome/fontawesome-free/css/all.min.css';

import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Avatar from 'primevue/avatar';
import Chip from 'primevue/chip';
import Menu from 'primevue/menu';
import Paginator from 'primevue/paginator';

// LƯU Ý: nếu bạn dùng JS, import kèm đuôi .js
import { fetchStaff, exportStaffExcel, importStaffExcel } from '@/service/staffService.js';
import CreateStaffModal from '@/components/staff/CreateStaffModal.vue'; // NEW
const showCreate = ref(false); // NEW
function onStaffCreated(newStaff) {
    // NEW
    // Thêm vào mock dữ liệu hiện tại để thấy ngay trên giao diện
    // Ưu tiên thêm lên đầu danh sách trang hiện tại
    rows.value = [newStaff, ...rows.value];
    // Cập nhật bộ đếm tổng
    allData.value = [newStaff, ...allData.value];
}
// state
const loading = ref(false);
const rows = ref([]);
const totalRecords = ref(0);

// table state
const selection = ref(null);
const page = ref(1);
const size = ref(10);
const sortField = ref('');
const sortOrder = ref(0);

// filters
const statusFilter = ref('all');

// header inputs
const fName = ref('');
const fUsername = ref('');
const fPhone = ref('');

// cache all (đếm chip – mock)
const allData = ref([]);
const counterAll = computed(() => allData.value.length);
const counterNever = computed(() => allData.value.filter((x) => x.status === 'neverLoggedIn').length);
const counterActive = computed(() => allData.value.filter((x) => x.status === 'active').length);
const counterLocked = computed(() => allData.value.filter((x) => x.status === 'locked').length);
const counterDeleted = computed(() => allData.value.filter((x) => x.status === 'deleted').length);

// row menu
const actionMenu = ref();
const actionItems = ref([
    { label: 'Xem chi tiết', icon: 'fa-regular fa-eye', command: () => onAction('view') },
    { label: 'Sửa nhân viên', icon: 'fa-regular fa-pen-to-square', command: () => onAction('edit') },
    { label: 'Đổi mật khẩu', icon: 'fa-regular fa-pen-to-square', command: () => onAction('edit') },
    { separator: true },
    { label: 'Khóa tài khoản', icon: 'fa-solid fa-lock', command: () => onAction('lock') },
    { label: 'Xóa', icon: 'fa-regular fa-trash-can', command: () => onAction('delete') }
]);
const actionTargetRow = ref(null);
function onAction(type) {
    // TODO: mở dialog hoặc gọi API khi có BE
}

function openRowMenu(event, row) {
    actionTargetRow.value = row;
    actionMenu.value.toggle(event);
}

// Helpers cho hiển thị pill
function roleLabel(role) {
    return role || '-';
}
function statusLabel(s) {
    if (s === 'active') return 'Đang hoạt động';
    if (s === 'locked') return 'Tạm khóa';
    if (s === 'deleted') return 'Đã xóa';
    return 'Chưa đăng nhập';
}
function statusClass(s) {
    return s === 'active' ? 'status--active' : s === 'locked' ? 'status--locked' : s === 'deleted' ? 'status--deleted' : 'status--never';
}

// debounce nhỏ cho header inputs
let t;
function debounce(fn, ms = 250) {
    clearTimeout(t);
    t = setTimeout(fn, ms);
}

// load data (mock qua service)
async function load() {
    loading.value = true;
    try {
        const all = await fetchStaff({ status: 'all', page: 1, size: 9999 });
        allData.value = all.items;

        const parts = [];
        if (fName.value) parts.push(fName.value);
        if (fUsername.value) parts.push(fUsername.value);
        if (fPhone.value) parts.push(fPhone.value);
        const q = parts.join(' ');

        const { items, total } = await fetchStaff({
            q: q || undefined,
            status: statusFilter.value,
            page: page.value,
            size: size.value,
            sort: sortField.value ? `${sortField.value},${sortOrder.value === -1 ? 'desc' : 'asc'}` : undefined
        });

        rows.value = items;
        totalRecords.value = statusFilter.value === 'all' && !q ? all.total : items.length;
    } finally {
        loading.value = false;
    }
}

function onChangePage(e) {
    page.value = e.page + 1;
    size.value = e.rows;
    load();
}
function onSort(field) {
    if (sortField.value === field) sortOrder.value = sortOrder.value === 1 ? -1 : 1;
    else {
        sortField.value = field;
        sortOrder.value = 1;
    }
    load();
}
function setStatusFilter(s) {
    statusFilter.value = s;
    page.value = 1;
    load();
}

async function onExport() {
    await exportStaffExcel();
}
const fileInput = ref(null);
function triggerImport() {
    fileInput.value?.click();
}
async function onImportChange(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    await importStaffExcel(file);
    await load();
}

watch([fName, fUsername, fPhone], () =>
    debounce(() => {
        page.value = 1;
        load();
    }, 300)
);
onMounted(load);
</script>

<template>
    <div class="px-4 md:px-6 lg:px-8 py-5 space-y-5">
        <!-- Title -->
        <div class="rounded-xl bg-slate-50 border border-slate-200 px-4 py-3">
            <h1 class="text-xl font-semibold text-slate-800">Nhân viên</h1>
        </div>

        <!-- Chips + actions -->
        <div class="flex flex-wrap items-center gap-3 justify-between">
            <div class="flex flex-wrap items-center gap-2">
                <button class="chip chip--soft" :class="{ 'chip--active': statusFilter === 'all' }" @click="setStatusFilter('all')">
                    <span class="chip__icon"><i class="fa-regular fa-clock"></i></span>
                    <span class="font-medium">{{ counterAll }} nhân viên</span>
                </button>
                <button class="chip chip--gray" :class="{ 'chip--active': statusFilter === 'neverLoggedIn' }" @click="setStatusFilter('neverLoggedIn')">Chưa đăng nhập - {{ counterNever }}</button>
                <button class="chip chip--blue" :class="{ 'chip--active': statusFilter === 'active' }" @click="setStatusFilter('active')">Hoạt động - {{ counterActive }}</button>
                <button class="chip chip--amber" :class="{ 'chip--active': statusFilter === 'locked' }" @click="setStatusFilter('locked')">Tạm khóa - {{ counterLocked }}</button>
                <button class="chip chip--red" :class="{ 'chip--active': statusFilter === 'deleted' }" @click="setStatusFilter('deleted')">Đã bị xóa - {{ counterDeleted }}</button>
            </div>

            <div class="flex items-center gap-2">
                <!-- Thay nút Tạo nhân viên để mở modal -->
                <Button class="!bg-primary !border-0 !text-white" icon="fa-solid fa-plus mr-2" label="Tạo nhân viên" @click="showCreate = true" />
                <Button class="!bg-emerald-600 !border-0 !text-white" icon="fa-solid fa-file-arrow-up mr-2" label="Nhập excel" @click="triggerImport" />
                <input ref="fileInput" type="file" accept=".xlsx,.xls,.csv" class="hidden" @change="onImportChange" />
                <Button class="!bg-green-600 !border-0 !text-white" icon="fa-solid fa-file-arrow-down mr-2" label="Xuất excel" @click="onExport" />
            </div>
        </div>

        <!-- Bảng -->
        <div class="rounded-xl border border-slate-200 overflow-hidden bg-white">
            <DataTable :value="rows" dataKey="id" v-model:selection="selection" :loading="loading" :rows="size" responsiveLayout="scroll" :rowHover="true" class="p-datatable-sm staff-table">
                <Column selectionMode="multiple" headerStyle="width: 3rem" />
                <Column header="#" :body="(_, opt) => opt.rowIndex + 1" headerStyle="width: 4rem" />

                <!-- Tên nhân viên -->
                <Column>
                    <template #header>
                        <div class="header-filter">
                            <InputText v-model="fName" class="w-full" placeholder="Tên nhân viên" />
                            <button class="sort-btn" @click="onSort('name')" :aria-label="'Sắp xếp tên'">
                                <i class="fa-solid fa-up-down"></i>
                            </button>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <div class="flex items-center gap-3">
                            <Avatar :label="(data.name?.[0] ?? 'N').toUpperCase()" class="!bg-slate-100 !text-slate-700" shape="circle" />
                            <div class="min-w-0">
                                <div class="font-medium text-slate-900 truncate">{{ data.name }}</div>
                                <div class="text-slate-500 text-sm truncate">{{ data.email }}</div>
                            </div>
                        </div>
                    </template>
                </Column>

                <!-- Tài khoản -->
                <Column>
                    <template #header>
                        <div class="header-filter">
                            <InputText v-model="fUsername" class="w-full" placeholder="Tài khoản" />
                            <button class="sort-btn" @click="onSort('username')" :aria-label="'Sắp xếp tài khoản'">
                                <i class="fa-solid fa-up-down"></i>
                            </button>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <span class="font-medium">{{ data.username }}</span>
                    </template>
                </Column>

                <!-- Số điện thoại -->
                <Column>
                    <template #header>
                        <div class="header-filter">
                            <InputText v-model="fPhone" class="w-full" placeholder="Số điện thoại" />
                            <button class="sort-btn" @click="onSort('phone')" :aria-label="'Sắp xếp số điện thoại'">
                                <i class="fa-solid fa-up-down"></i>
                            </button>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <span>{{ data.phone || '-' }}</span>
                    </template>
                </Column>

                <!-- Lớp -->
                <Column header="Lớp" :bodyStyle="{ 'min-width': '16rem' }">
                    <template #body="{ data }">
                        <div class="flex flex-wrap gap-2">
                            <Chip v-for="(c, i) in data.classes || []" :key="i" :label="c" class="!bg-slate-100 !text-slate-700" />
                            <span v-if="!data.classes || data.classes.length === 0" class="text-slate-400">-</span>
                        </div>
                    </template>
                </Column>

                <!-- Chức vụ -->
                <Column header="Chức vụ" headerStyle="width: 160px" bodyStyle="width: 160px" bodyClass="cell-tight">
                    <template #body="{ data }">
                        <span class="pill pill--role" :title="roleLabel(data.role)">
                            <span class="pill__text">{{ roleLabel(data.role) }}</span>
                        </span>
                    </template>
                </Column>

                <!-- Trạng thái -->
                <Column header="Trạng thái" headerStyle="width: 160px" bodyStyle="width: 160px" bodyClass="cell-tight">
                    <template #body="{ data }">
                        <span class="pill pill--status" :class="statusClass(data.status)" :title="statusLabel(data.status)">
                            <span class="pill__text">{{ statusLabel(data.status) }}</span>
                        </span>
                    </template>
                </Column>

                <!-- Hành động -->
                <Column header="Hành động" headerStyle="width: 6rem; text-align: right; white-space: nowrap;" bodyStyle="text-align: right;">
                    <template #body="{ data }">
                        <Button icon="fa-solid fa-ellipsis-vertical" class="!bg-transparent !border-0 !text-slate-600 hover:!bg-slate-100" @click="(e) => openRowMenu(e, data)" aria-haspopup="true" aria-controls="row_actions_menu" />
                    </template>
                </Column>
            </DataTable>

            <div class="border-top">
                <Paginator :rows="size" :totalRecords="totalRecords" :rowsPerPageOptions="[10, 20, 50]" @page="onChangePage" />
            </div>
        </div>

        <Menu ref="actionMenu" id="row_actions_menu" :model="actionItems" :popup="true">
            <template #item="{ item, label }">
                <a class="p-menuitem-link">
                    <i :class="['mr-2', item.icon]"></i>
                    <span>{{ label }}</span>
                </a>
            </template>
        </Menu>
    </div>
    <CreateStaffModal v-model:modelValue="showCreate" @created="onStaffCreated" />
</template>

<style scoped>
/* Chips (trên cùng) */
.chip {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: #f1f5f9;
    color: #0f172a;
    border: 1px solid #e2e8f0;
    border-radius: 9999px;
    font-size: 14px;
    line-height: 1;
}
.chip--soft {
    background: #eef2ff;
    border-color: #dbeafe;
}
.chip--gray {
    background: #f8fafc;
    border-color: #e2e8f0;
}
.chip--blue {
    background: #e0efff;
    border-color: #bfdbfe;
}
.chip--amber {
    background: #fff7ed;
    border-color: #fed7aa;
}
.chip--red {
    background: #fef2f2;
    border-color: #fecaca;
}
.chip--active {
    border-color: #94a3b8;
}
.chip__icon {
    width: 22px;
    height: 22px;
    border-radius: 9999px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    color: #2563eb;
    border: 1px solid #bfdbfe;
}

/* Ô "gọn" để canh đều pill trong cell */
.cell-tight {
    /* Căn giữa theo trục dọc nếu hàng cao hơn */
    display: table-cell;
    vertical-align: middle;
}

/* Pill thống nhất chiều cao – chống xuống dòng */
.pill {
    display: inline-flex;
    align-items: center;
    max-width: 100%;
    height: 28px;
    padding: 0 10px;
    border-radius: 9999px;
    font-size: 13px;
    font-weight: 600;
    line-height: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    gap: 6px;
}

/* Chức vụ: màu trung tính */
.pill--role {
    background: #eef2f7; /* nhẹ, trung tính */
    color: #0f172a;
    border: 1px solid #e2e8f0;
}

/* Trạng thái: đồng nhất màu/chiều cao */
.pill--status {
    border: 1px solid transparent;
}
.pill--status.status--active {
    background: #e6f0ff;
    color: #1d4ed8;
    border-color: #bfdbfe;
}
.pill--status.status--locked {
    background: #e7f7ef;
    color: #047857;
    border-color: #a7f3d0;
}
.pill--status.status--deleted {
    background: #fee2e2;
    color: #b91c1c;
    border-color: #fecaca;
}
.pill--status.status--never {
    background: #eef2f7;
    color: #334155;
    border-color: #e2e8f0;
}

/* Text trong pill để ellipsis mượt */
.pill__text {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* Header filter cell */
.header-filter {
    display: flex;
    align-items: center;
    gap: 8px;
}
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

.border-top {
    border-top: 1px solid #e5e7eb;
}

/* PrimeVue head cell tone */
:deep(.p-datatable .p-datatable-thead > tr > th) {
    background: #fff;
}
</style>
