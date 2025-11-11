<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import '@fortawesome/fontawesome-free/css/all.min.css';

import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import Paginator from 'primevue/paginator';
import Menu from 'primevue/menu';
import ConfirmDialog from 'primevue/confirmdialog';

import { fetchParents, exportParentsExcel, lockParents, unlockParents, deleteParents } from '@/service/parentService.js';
import ParentUpsertModal from '@/components/staff/ParentUpsertModal.vue';

const loading = ref(false);
const rows = ref([]);
const totalRecords = ref(0);

const page = ref(1);
const size = ref(10);
const sortField = ref('');
const sortOrder = ref(1);

const fSearch = ref(''); // tìm theo tên/sđt/email
const fStatus = ref({ label: 'Tất cả', value: 'all' });
const statusOptions = [
    { label: 'Tất cả', value: 'all' },
    { label: 'Hoạt động', value: 'active' },
    { label: 'Đã khóa', value: 'blocked' }
];

const selection = ref([]);
const rowMenu = ref();
const rowMenuModel = ref([
    { label: 'Xem chi tiết', icon: 'fa-regular fa-eye', command: (e) => onView(e) },
    { label: 'Sửa', icon: 'fa-regular fa-pen-to-square', command: (e) => onEdit(e) },
    { separator: true },
    { label: 'Khóa tài khoản', icon: 'fa-solid fa-lock', command: (e) => onLock(e) },
    { label: 'Mở khóa', icon: 'fa-solid fa-unlock', command: (e) => onUnlock(e) },
    { separator: true },
    { label: 'Xóa', icon: 'fa-regular fa-trash-can', class: 'text-rose-600', command: (e) => onDelete(e) }
]);
const activeRow = ref(null);

function openRowMenu(event, row) {
    activeRow.value = row;
    rowMenu.value.toggle(event);
}

const counts = ref({ total: 0, active: 0, blocked: 0 });
const statusTabs = computed(() => [
    { key: 'all', label: `Tất cả (${counts.value.total})`, bg: '' },
    { key: 'active', label: `Hoạt động (${counts.value.active})`, bg: 'tab--green' },
    { key: 'blocked', label: `Đã khóa (${counts.value.blocked})`, bg: 'tab--red' }
]);
const currentTab = ref('all');

function statusSeverity(row) {
    return row.statusKey === 'blocked' ? 'danger' : 'success';
}
function statusText(row) {
    return row.status || (row.statusKey === 'blocked' ? 'Đã khóa' : 'Hoạt động');
}

async function load() {
    loading.value = true;
    try {
        const sort = sortField.value ? `${sortField.value},${sortOrder.value === -1 ? 'desc' : 'asc'}` : undefined;
        const {
            items,
            total,
            counts: cts
        } = await fetchParents({
            q: fSearch.value || undefined,
            status: currentTab.value,
            page: page.value,
            size: size.value,
            sort
        });
        rows.value = items;
        totalRecords.value = total;
        counts.value = cts;
    } finally {
        loading.value = false;
    }
}

function onSort(field) {
    if (sortField.value === field) {
        sortOrder.value = sortOrder.value === 1 ? -1 : 1;
    } else {
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

function switchTab(k) {
    currentTab.value = k;
    page.value = 1;
    load();
}
async function onExport() {
    await exportParentsExcel();
}

/* Row actions */
function onView() {
    // mở trang chi tiết nếu bạn có route riêng
    // this.$router.push({ name:'ParentDetail', params:{ id: activeRow.value.id }});
}
const showUpsert = ref(false);
const editing = ref(null);
function onCreate() {
    editing.value = null;
    showUpsert.value = true;
}
function onEdit() {
    editing.value = activeRow.value;
    showUpsert.value = true;
}

async function onAfterUpsert() {
    showUpsert.value = false;
    await load();
}

async function onLock() {
    const ids = [activeRow.value?.id].filter(Boolean);
    if (!ids.length) return;
    await lockParents(ids);
    await load();
}
async function onUnlock() {
    const ids = [activeRow.value?.id].filter(Boolean);
    if (!ids.length) return;
    await unlockParents(ids);
    await load();
}
async function onDelete() {
    const ids = [activeRow.value?.id].filter(Boolean);
    if (!ids.length) return;
    await deleteParents(ids);
    await load();
}

/* Bulk */
async function doBulk(action) {
    const ids = selection.value.map((x) => x.id);
    if (!ids.length) return;
    if (action === 'lock') await lockParents(ids);
    if (action === 'unlock') await unlockParents(ids);
    if (action === 'delete') await deleteParents(ids);
    selection.value = [];
    await load();
}

/* Debounce search */
let t;
function debounce(fn, ms = 250) {
    clearTimeout(t);
    t = setTimeout(fn, ms);
}
watch([fSearch], () =>
    debounce(() => {
        page.value = 1;
        load();
    }, 300)
);

onMounted(load);
</script>

<template>
    <div class="px-4 md:px-6 lg:px-8 py-5 space-y-4">
        <!-- Header -->
        <div class="flex flex-wrap items-center justify-between gap-3">
            <h1 class="text-xl font-semibold text-slate-800">Phụ huynh</h1>
            <div class="flex items-center gap-2">
                <Button class="!bg-green-600 !border-0 !text-white" icon="fa-solid fa-file-arrow-down mr-2" label="Xuất excel" @click="onExport" />
                <Button class="!bg-primary !border-0 !text-white" icon="fa-solid fa-plus mr-2" label="Thêm phụ huynh" @click="onCreate" />
            </div>
        </div>

        <!-- Tabs -->
        <div class="flex flex-wrap items-center gap-3 border-b border-slate-200">
            <button v-for="t in statusTabs" :key="t.key" class="tab" :class="[currentTab === t.key ? 'tab--active' : '', t.bg]" @click="switchTab(t.key)">
                <span>{{ t.label }}</span>
            </button>
        </div>

        <!-- Filters -->
        <div class="flex flex-wrap items-center gap-3">
            <div class="flex-1 min-w-[240px]">
                <InputText v-model="fSearch" class="w-full" placeholder="Tìm tên/SĐT/Email..." />
            </div>
            <div class="flex items-center gap-2">
                <Button class="!bg-amber-500 !border-0 !text-white" icon="fa-solid fa-lock mr-2" label="Khóa" @click="doBulk('lock')" />
                <Button class="!bg-emerald-600 !border-0 !text-white" icon="fa-solid fa-unlock mr-2" label="Mở khóa" @click="doBulk('unlock')" />
                <Button class="!bg-rose-600 !border-0 !text-white" icon="fa-regular fa-trash-can mr-2" label="Xóa" @click="doBulk('delete')" />
            </div>
        </div>

        <!-- Table -->
        <div class="rounded-xl border border-slate-200 overflow-hidden bg-white">
            <DataTable :value="rows" v-model:selection="selection" dataKey="id" :loading="loading" :rows="size" responsiveLayout="scroll" :rowHover="true" class="p-datatable-sm">
                <Column selectionMode="multiple" headerStyle="width: 3rem" />
                <Column header="#" :body="(_, opt) => opt.rowIndex + 1" headerStyle="width: 4rem" />

                <Column>
                    <template #header>
                        <div class="header-filter">
                            <span class="font-semibold">Họ và tên</span>
                            <button class="sort-btn" @click="onSort('name')" title="Sắp xếp theo tên"><i class="fa-solid fa-up-down"></i></button>
                        </div>
                    </template>
                    <template #body="{ data }">
                        <div class="font-semibold text-slate-900">{{ data.name }}</div>
                        <div class="text-slate-500 text-sm">@{{ data.username }}</div>
                    </template>
                </Column>

                <Column header="Liên hệ">
                    <template #body="{ data }">
                        <div class="text-slate-900"><i class="fa-solid fa-phone"></i> {{ data.phone || '-' }}</div>
                        <div class="text-slate-500 text-sm"><i class="fa-regular fa-envelope"></i> {{ data.email || '-' }}</div>
                    </template>
                </Column>

                <Column header="Con đang học">
                    <template #body="{ data }">
                        <div class="flex flex-wrap gap-1">
                            <Tag v-for="(st, idx) in data.studentNames" :key="idx" class="!bg-slate-100 !text-slate-700 !border-0">
                                {{ st }}
                            </Tag>
                            <span v-if="!data.studentNames?.length" class="text-slate-400">-</span>
                        </div>
                    </template>
                </Column>

                <Column header="Trạng thái" headerStyle="width: 140px;">
                    <template #body="{ data }">
                        <Tag :severity="statusSeverity(data)">{{ statusText(data) }}</Tag>
                    </template>
                </Column>

                <Column header="Hành động" headerStyle="width: 64px; text-align: right;" bodyStyle="text-align: right;">
                    <template #body="{ data }">
                        <Button icon="fa-solid fa-ellipsis-vertical" class="!bg-transparent !border-0 !text-slate-600 hover:!bg-slate-100" @click="(e) => openRowMenu(e, data)" />
                    </template>
                </Column>
            </DataTable>

            <div class="border-t border-slate-200">
                <Paginator :rows="size" :totalRecords="totalRecords" :rowsPerPageOptions="[10, 20, 50]" @page="onChangePage" />
            </div>
        </div>

        <Menu ref="rowMenu" :model="rowMenuModel" :popup="true" appendTo="body" />

        <ParentUpsertModal v-model:modelValue="showUpsert" :parent="editing" @saved="onAfterUpsert" />
        <ConfirmDialog />
    </div>
</template>

<style scoped>
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
.tab--green.tab--active {
    background: #bbf7d0;
}
.tab--red.tab--active {
    background: #fecaca;
}

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
