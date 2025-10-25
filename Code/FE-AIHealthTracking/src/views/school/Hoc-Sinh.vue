<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import '@fortawesome/fontawesome-free/css/all.min.css';

import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Avatar from 'primevue/avatar';
import Menu from 'primevue/menu';
import Paginator from 'primevue/paginator';

// LƯU Ý: đang dùng JS => import kèm .js
import { fetchStudents, exportStudentsExcel, importStudentsExcel } from '@/service/studentService.js';

/* Top filters */
const years = ref([
    { label: '2024 - 2025', value: '2024-2025' },
    { label: '2023 - 2024', value: '2023-2024' }
]);
const selectedYear = ref(years.value[0]);

const grades = ref([
    { label: 'Chọn khối', value: '' },
    { label: 'Koala', value: 'koala' },
    { label: 'Panda', value: 'panda' },
    { label: 'Polar', value: 'polar' },
    { label: 'Global', value: 'global' }
]);
const selectedGrade = ref(grades.value[0]);

const groups = ref([
    { label: 'Chọn nhóm lớp', value: '' },
    { label: 'Nhóm 1', value: 'g1' },
    { label: 'Nhóm 2', value: 'g2' }
]);
const selectedGroup = ref(groups.value[0]);

const systems = ref([
    { label: 'Chọn hệ lớp', value: '' },
    { label: 'Song ngữ', value: 'bi' },
    { label: 'Quốc tế', value: 'intl' }
]);
const selectedSystem = ref(systems.value[0]);

/* Status tabs */
const status = ref('studying'); // studying | waiting | reserved | dropped | graduated | deleted | all
const statusDefs = [
    { key: 'studying', label: 'Đang đi học', color: 'text-primary', bg: 'tab--blue' },
    { key: 'waiting', label: 'Chờ phân lớp', color: 'text-amber-600', bg: 'tab--amber' },
    { key: 'reserved', label: 'Đã bảo lưu', color: 'text-orange-600', bg: 'tab--orange' },
    { key: 'dropped', label: 'Đã thôi học', color: 'text-rose-600', bg: 'tab--rose' },
    { key: 'graduated', label: 'Đã tốt nghiệp', color: 'text-emerald-600', bg: 'tab--green' },
    { key: 'deleted', label: 'Đã xóa', color: 'text-rose-600', bg: 'tab--red' }
];

/* Table filters in headers */
const fCode = ref('');
const fName = ref('');
const fClass = ref('');
const fParent = ref('');

/* Paging/sort */
const page = ref(1);
const size = ref(10);
const sortField = ref('');
const sortOrder = ref(0);

/* Data */
const loading = ref(false);
const rows = ref([]);
const totalRecords = ref(0);
const selection = ref([]); // selected rows

/* Counters */
const allData = ref([]); // full dataset for counting (mock)
const counts = computed(() => ({
    studying: allData.value.filter((x) => x.status === 'studying').length,
    waiting: allData.value.filter((x) => x.status === 'waiting').length,
    reserved: allData.value.filter((x) => x.status === 'reserved').length,
    dropped: allData.value.filter((x) => x.status === 'dropped').length,
    graduated: allData.value.filter((x) => x.status === 'graduated').length,
    deleted: allData.value.filter((x) => x.status === 'deleted').length,
    total: allData.value.length
}));

/* Row actions menu */
const rowMenu = ref();
const rowMenuItems = ref([
    { label: 'Xem hồ sơ', icon: 'fa-regular fa-id-card', command: () => {} },
    { label: 'Chuyển lớp', icon: 'fa-solid fa-right-left', command: () => {} },
    { label: 'Chuyển trường', icon: 'fa-solid fa-school-flag', command: () => {} },
    { separator: true },
    { label: 'Thôi học', icon: 'fa-solid fa-user-minus', command: () => {} },
    { label: 'Bảo lưu', icon: 'fa-solid fa-tent-arrow-left-right', command: () => {} },
    { label: 'Xóa học sinh', icon: 'fa-regular fa-trash-can', command: () => {} }
]);
const activeRow = ref(null);
function openRowMenu(e, row) {
    activeRow.value = row;
    rowMenu.value.toggle(e);
}

/* Helpers */
function genderIcon(g) {
    return g === 'F' ? 'fa-solid fa-venus text-pink-500' : 'fa-solid fa-mars text-blue-500';
}
function formatDob(d) {
    try {
        const dt = new Date(d);
        const dd = dt.getDate().toString().padStart(2, '0');
        const mm = (dt.getMonth() + 1).toString().padStart(2, '0');
        const yyyy = dt.getFullYear();
        return `${dd}/${mm}/${yyyy}`;
    } catch {
        return d;
    }
}

/* Load data (mock) */
async function load() {
    loading.value = true;
    try {
        // fetch all for counters
        const all = await fetchStudents({ status: 'all', page: 1, size: 9999 });
        allData.value = all.items;

        const sort = sortField.value ? `${sortField.value},${sortOrder.value === -1 ? 'desc' : 'asc'}` : undefined;

        const { items, total } = await fetchStudents({
            status: status.value,
            year: selectedYear.value?.value,
            grade: selectedGrade.value?.value,
            group: selectedGroup.value?.value,
            system: selectedSystem.value?.value,
            code: fCode.value || undefined,
            name: fName.value || undefined,
            className: fClass.value || undefined,
            parentName: fParent.value || undefined,
            page: page.value,
            size: size.value,
            sort
        });

        rows.value = items;
        totalRecords.value = status.value === 'all' && !fCode.value && !fName.value && !fClass.value && !fParent.value ? all.total : total; // mock behavior
    } finally {
        loading.value = false;
    }
}

function onStatusChange(k) {
    status.value = k;
    page.value = 1;
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
function onChangePage(e) {
    page.value = e.page + 1;
    size.value = e.rows;
    load();
}

/* Top actions */
function onBulk(action) {
    // TODO: thực thi theo selection.value
    // console.log(action, selection.value);
}
async function onExport() {
    await exportStudentsExcel();
}
const fileInput = ref(null);
function triggerImport() {
    fileInput.value?.click();
}
async function onImportChange(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    await importStudentsExcel(file);
    await load();
}

/* React to filters (debounced for header inputs) */
let t;
function debounce(fn, ms = 250) {
    clearTimeout(t);
    t = setTimeout(fn, ms);
}

watch([fCode, fName, fClass, fParent], () =>
    debounce(() => {
        page.value = 1;
        load();
    }, 300)
);
watch([selectedYear, selectedGrade, selectedGroup, selectedSystem], () => {
    page.value = 1;
    load();
});

onMounted(load);
</script>

<template>
    <div class="px-4 md:px-6 lg:px-8 py-5 space-y-4">
        <!-- Title + total + import/export -->
        <div class="flex flex-wrap items-center justify-between gap-3">
            <h1 class="text-xl font-semibold text-slate-800">Học sinh</h1>
            <div class="flex items-center gap-2">
                <div class="inline-flex items-center gap-2 px-3 py-2 rounded-lg bg-slate-50 border border-slate-200 text-slate-700">
                    <i class="fa-regular fa-clock text-primary"></i>
                    <span class="font-semibold">{{ counts.total }} học sinh</span>
                </div>
                <Button class="!bg-emerald-600 !border-0 !text-white" icon="fa-solid fa-file-arrow-up mr-2" label="Nhập excel" @click="triggerImport" />
                <input ref="fileInput" type="file" accept=".xlsx,.xls,.csv" class="hidden" @change="onImportChange" />
                <Button class="!bg-green-600 !border-0 !text-white" icon="fa-solid fa-file-arrow-down mr-2" label="Xuất excel" @click="onExport" />
            </div>
        </div>

        <!-- Top filter row -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
            <Dropdown v-model="selectedYear" :options="years" optionLabel="label" class="w-full" placeholder="Năm học" />
            <Dropdown v-model="selectedGrade" :options="grades" optionLabel="label" class="w-full" />
            <Dropdown v-model="selectedGroup" :options="groups" optionLabel="label" class="w-full" />
            <Dropdown v-model="selectedSystem" :options="systems" optionLabel="label" class="w-full" />
        </div>

        <!-- Status tabs -->
        <div class="flex flex-wrap items-center gap-4 border-b border-slate-200">
            <button v-for="s in statusDefs" :key="s.key" class="tab" :class="[status === s.key ? 'tab--active' : '', s.bg]" @click="onStatusChange(s.key)">
                <span>{{ s.label }}</span>
                <span class="tab__badge">{{ counts[s.key] || 0 }}</span>
            </button>
        </div>

        <!-- Bulk actions -->
        <div class="flex flex-wrap items-center gap-2">
            <Button class="!bg-amber-500 !border-0 !text-white" icon="fa-solid fa-right-left mr-2" label="Chuyển lớp" @click="onBulk('class')" />
            <Button class="!bg-sky-500 !border-0 !text-white" icon="fa-solid fa-school-flag mr-2" label="Chuyển trường" @click="onBulk('school')" />
            <Button class="!bg-rose-500 !border-0 !text-white" icon="fa-solid fa-user-minus mr-2" label="Thôi học" @click="onBulk('drop')" />
            <Button class="!bg-orange-500 !border-0 !text-white" icon="fa-solid fa-tent-arrow-left-right mr-2" label="Bảo lưu" @click="onBulk('reserve')" />
            <Button class="!bg-red-600 !border-0 !text-white" icon="fa-regular fa-trash-can mr-2" label="Xóa học sinh" @click="onBulk('delete')" />
            <span class="flex-1"></span>
            <Button class="!bg-primary !border-0 !text-white" icon="fa-solid fa-plus mr-2" label="Tạo học sinh" />
        </div>

        <!-- Table -->
        <div class="rounded-xl border border-slate-200 overflow-hidden bg-white">
            <DataTable :value="rows" v-model:selection="selection" dataKey="id" :loading="loading" :rows="size" responsiveLayout="scroll" :rowHover="true" class="p-datatable-sm">
                <Column selectionMode="multiple" headerStyle="width: 3rem" />
                <Column header="#" :body="(_, opt) => opt.rowIndex + 1" headerStyle="width: 4rem" />

                <!-- Mã học sinh -->
                <Column>
                    <template #header>
                        <div class="header-filter">
                            <InputText v-model="fCode" class="w-full" placeholder="Mã học sinh" />
                            <button class="sort-btn" @click="onSort('code')" title="Sắp xếp theo mã">
                                <i class="fa-solid fa-up-down"></i>
                            </button>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <span class="font-medium">{{ data.code || '-' }}</span>
                    </template>
                </Column>

                <!-- Tên học sinh -->
                <Column>
                    <template #header>
                        <div class="header-filter">
                            <InputText v-model="fName" class="w-full" placeholder="Tên học sinh" />
                            <button class="sort-btn" @click="onSort('name')" title="Sắp xếp theo tên">
                                <i class="fa-solid fa-up-down"></i>
                            </button>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <div class="flex items-center gap-3">
                            <Avatar :label="(data.name?.[0] ?? 'H').toUpperCase()" class="!bg-slate-100 !text-slate-700" />
                            <div class="min-w-0">
                                <div class="font-semibold text-slate-900 truncate hover:underline cursor-pointer">{{ data.name }}</div>
                                <div class="text-slate-500 text-sm flex items-center gap-3">
                                    <span><i class="fa-regular fa-calendar"></i> {{ formatDob(data.dob) }}</span>
                                    <span class="flex items-center gap-1"><i :class="genderIcon(data.gender)"></i> {{ data.gender === 'F' ? 'Nữ' : 'Nam' }}</span>
                                </div>
                            </div>
                        </div>
                    </template>
                </Column>

                <!-- Lớp -->
                <Column header="Lớp">
                    <template #header>
                        <div class="header-filter">
                            <InputText v-model="fClass" class="w-full" placeholder="Lớp" />
                            <button class="sort-btn" @click="onSort('className')" title="Sắp xếp theo lớp">
                                <i class="fa-solid fa-up-down"></i>
                            </button>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <span class="font-medium">{{ data.className || '-' }}</span>
                    </template>
                </Column>

                <!-- Tên phụ huynh -->
                <Column>
                    <template #header>
                        <div class="header-filter">
                            <InputText v-model="fParent" class="w-full" placeholder="Tên phụ huynh" />
                            <button class="sort-btn" @click="onSort('parentName')" title="Sắp xếp theo phụ huynh">
                                <i class="fa-solid fa-up-down"></i>
                            </button>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <div class="text-slate-900">
                            {{ data.parentName || '-' }}
                        </div>
                        <div class="text-slate-500 text-sm"><i class="fa-solid fa-phone"></i> {{ data.parentPhone || '-' }}</div>
                    </template>
                </Column>

                <!-- Hành động -->
                <Column header="Hành động" headerStyle="width: 6rem; text-align: right;" bodyStyle="text-align: right;">
                    <template #body="{ data }">
                        <Button icon="fa-solid fa-ellipsis-vertical" class="!bg-transparent !border-0 !text-slate-600 hover:!bg-slate-100" @click="(e) => openRowMenu(e, data)" />
                    </template>
                </Column>
            </DataTable>

            <!-- Paginator -->
            <div class="border-t border-slate-200">
                <Paginator :rows="size" :totalRecords="totalRecords" :rowsPerPageOptions="[10, 20, 50]" @page="onChangePage" />
            </div>
        </div>

        <!-- Row menu -->
        <Menu ref="rowMenu" :model="rowMenuItems" :popup="true">
            <template #item="{ item }">
                <a class="p-menuitem-link"
                    ><i :class="['mr-2', item.icon]"></i><span>{{ item.label }}</span></a
                >
            </template>
        </Menu>
    </div>
</template>

<style scoped>
/* Status tabs */
.tab {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    color: #0f172a;
    border-bottom: 2px solid transparent;
    border-radius: 8px 8px 0 0;
    background: transparent;
}
.tab--active {
    border-color: #2563eb;
    color: #1d4ed8;
    background: #eef2ff;
}
.tab__badge {
    display: inline-flex;
    min-width: 22px;
    height: 22px;
    padding: 0 8px;
    border-radius: 9999px;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    background: #e5e7eb;
}

/* subtle bg per tab type (optional) */
.tab--blue.tab--active .tab__badge {
    background: #dbeafe;
}
.tab--amber.tab--active .tab__badge {
    background: #fde68a;
}
.tab--orange.tab--active .tab__badge {
    background: #ffedd5;
}
.tab--rose.tab--active .tab__badge {
    background: #fecdd3;
}
.tab--green.tab--active .tab__badge {
    background: #bbf7d0;
}
.tab--red.tab--active .tab__badge {
    background: #fecaca;
}

/* Header filter with sort button */
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
</style>
